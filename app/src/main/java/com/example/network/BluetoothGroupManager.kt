package com.example.network

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class DiscoveredBluetoothDevice(
    val name: String,
    val address: String,
    val isPaired: Boolean
)

sealed interface BluetoothEvent {
    data class ClientConnected(val clientId: String, val deviceName: String) : BluetoothEvent
    data class ClientMessage(val clientId: String, val envelope: NetworkEnvelope) : BluetoothEvent
    data class ClientDisconnected(val clientId: String) : BluetoothEvent
    data class ConnectedToHost(val hostName: String) : BluetoothEvent
    data class HostMessage(val envelope: NetworkEnvelope) : BluetoothEvent
    data class DisconnectedFromHost(val reason: String) : BluetoothEvent
    data class Error(val message: String) : BluetoothEvent
}

class BluetoothGroupManager(private val context: Context) {

    companion object {
        val HOKM_SERVICE_UUID: UUID = UUID.fromString("8a4d2c10-6b7e-4e9a-9f12-3c5d7e8f0a1b")
        const val SERVICE_NAME = "HokmBluetoothGroup"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val adapter: BluetoothAdapter? get() = bluetoothManager?.adapter

    private val _events = MutableSharedFlow<BluetoothEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<BluetoothEvent> = _events.asSharedFlow()

    private val _pairedDevices = MutableStateFlow<List<DiscoveredBluetoothDevice>>(emptyList())
    val pairedDevices: StateFlow<List<DiscoveredBluetoothDevice>> = _pairedDevices.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredBluetoothDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<DiscoveredBluetoothDevice>> = _discoveredDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _isHosting = MutableStateFlow(false)
    val isHosting: StateFlow<Boolean> = _isHosting.asStateFlow()

    private var serverSocket: BluetoothServerSocket? = null
    private var acceptJob: Job? = null
    private val hostConnections = ConcurrentHashMap<String, SocketSession>()

    private var clientSession: SocketSession? = null
    private var clientConnectJob: Job? = null
    private var discoveryReceiverRegistered = false

    private val discoveryReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                    if (device != null) {
                        val name = if (hasConnectPermission()) {
                            device.name?.takeIf { it.isNotBlank() } ?: "دستگاه ناشناس"
                        } else {
                            "دستگاه بلوتوث"
                        }
                        val address = device.address ?: return
                        val isPaired = _pairedDevices.value.any { it.address == address }
                        val item = DiscoveredBluetoothDevice(name, address, isPaired)
                        val current = _discoveredDevices.value.toMutableList()
                        val idx = current.indexOfFirst { it.address == address }
                        if (idx >= 0) current[idx] = item else current.add(item)
                        _discoveredDevices.value = current
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isScanning.value = false
                }
            }
        }
    }

    fun isBluetoothSupported(): Boolean = adapter != null

    fun isBluetoothEnabled(): Boolean = adapter?.isEnabled == true

    fun requiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun hasAllPermissions(): Boolean {
        return requiredPermissions().all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun hasScanPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("MissingPermission")
    fun refreshPairedDevices() {
        val bt = adapter
        if (bt == null || !bt.isEnabled || !hasConnectPermission()) {
            _pairedDevices.value = emptyList()
            return
        }
        val bonded = bt.bondedDevices.orEmpty().map { device ->
            DiscoveredBluetoothDevice(
                name = device.name?.takeIf { it.isNotBlank() } ?: "دستگاه جفت‌شده",
                address = device.address,
                isPaired = true
            )
        }.sortedBy { it.name }
        _pairedDevices.value = bonded
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        val bt = adapter ?: run {
            _events.tryEmit(BluetoothEvent.Error("این دستگاه از بلوتوث پشتیبانی نمی‌کند."))
            return
        }
        if (!bt.isEnabled) {
            _events.tryEmit(BluetoothEvent.Error("لطفاً ابتدا بلوتوث گوشی را روشن کنید."))
            return
        }
        if (!hasScanPermission() || !hasConnectPermission()) {
            _events.tryEmit(BluetoothEvent.Error("دسترسی جستجو و اتصال بلوتوث داده نشده است."))
            return
        }
        refreshPairedDevices()
        registerReceiverIfNeeded()
        _discoveredDevices.value = emptyList()
        if (bt.isDiscovering) {
            bt.cancelDiscovery()
        }
        val started = bt.startDiscovery()
        _isScanning.value = started
        if (!started) {
            _events.tryEmit(BluetoothEvent.Error("شروع جستجوی دستگاه‌های اطراف ناموفق بود."))
        }
    }

    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        _isScanning.value = false
        val bt = adapter ?: return
        if (hasScanPermission() && bt.isDiscovering) {
            runCatching { bt.cancelDiscovery() }
        }
    }

    @SuppressLint("MissingPermission")
    fun startHosting() {
        val bt = adapter ?: run {
            _events.tryEmit(BluetoothEvent.Error("بلوتوث در دسترس نیست."))
            return
        }
        if (!bt.isEnabled) {
            _events.tryEmit(BluetoothEvent.Error("ابتدا بلوتوث را روشن کنید."))
            return
        }
        if (!hasConnectPermission()) {
            _events.tryEmit(BluetoothEvent.Error("دسترسی اتصال بلوتوث لازم است."))
            return
        }

        stopAll()
        _isHosting.value = true

        acceptJob = scope.launch {
            var server: BluetoothServerSocket? = null
            try {
                server = bt.listenUsingRfcommWithServiceRecord(SERVICE_NAME, HOKM_SERVICE_UUID)
                serverSocket = server
                while (_isHosting.value) {
                    val socket = server.accept()
                    val remoteName = runCatching {
                        if (hasConnectPermission()) socket.remoteDevice?.name else null
                    }.getOrNull() ?: "بازیکن بلوتوثی"
                    val clientId = socket.remoteDevice?.address ?: UUID.randomUUID().toString()

                    // Replace older session from same MAC address if reconnecting
                    hostConnections.remove(clientId)?.close()

                    val session = SocketSession(
                        id = clientId,
                        socket = socket,
                        onMessage = { line ->
                            HokmJsonCodec.decodeEnvelope(line)?.let { env ->
                                _events.tryEmit(BluetoothEvent.ClientMessage(clientId, env))
                            }
                        },
                        onClosed = {
                            hostConnections.remove(clientId)
                            _events.tryEmit(BluetoothEvent.ClientDisconnected(clientId))
                        }
                    )
                    hostConnections[clientId] = session
                    _events.tryEmit(BluetoothEvent.ClientConnected(clientId, remoteName))
                    session.startReading(scope)
                }
            } catch (e: IOException) {
                if (_isHosting.value) {
                    _events.tryEmit(BluetoothEvent.Error("خطا در سرور بلوتوث: ${e.localizedMessage ?: "قطع اتصال"}"))
                }
            } finally {
                _isHosting.value = false
                runCatching { server?.close() }
                serverSocket = null
            }
        }
    }

    fun sendToClient(clientId: String, jsonLine: String) {
        val session = hostConnections[clientId] ?: return
        scope.launch {
            session.sendLine(jsonLine)
        }
    }

    fun disconnectClient(clientId: String) {
        hostConnections.remove(clientId)?.close()
    }

    @SuppressLint("MissingPermission")
    fun connectToHost(deviceAddress: String, playerId: String, playerName: String) {
        val bt = adapter ?: run {
            _events.tryEmit(BluetoothEvent.Error("بلوتوث در دسترس نیست."))
            return
        }
        if (!bt.isEnabled) {
            _events.tryEmit(BluetoothEvent.Error("ابتدا بلوتوث را روشن کنید."))
            return
        }
        if (!hasConnectPermission()) {
            _events.tryEmit(BluetoothEvent.Error("دسترسی اتصال بلوتوث داده نشده است."))
            return
        }

        stopDiscovery()
        clientConnectJob?.cancel()
        clientSession?.close()
        clientSession = null

        clientConnectJob = scope.launch {
            try {
                val device = bt.getRemoteDevice(deviceAddress)
                val hostDisplayName = device.name?.takeIf { it.isNotBlank() } ?: "میزبان"
                if (hasScanPermission() && bt.isDiscovering) {
                    bt.cancelDiscovery()
                }
                val socket = device.createRfcommSocketToServiceRecord(HOKM_SERVICE_UUID)
                socket.connect()

                val session = SocketSession(
                    id = "host",
                    socket = socket,
                    onMessage = { line ->
                        HokmJsonCodec.decodeEnvelope(line)?.let { env ->
                            _events.tryEmit(BluetoothEvent.HostMessage(env))
                        }
                    },
                    onClosed = {
                        clientSession = null
                        _events.tryEmit(BluetoothEvent.DisconnectedFromHost("ارتباط با میزبان قطع شد."))
                    }
                )
                clientSession = session
                session.startReading(scope)
                session.sendLine(HokmJsonCodec.encodeJoin(JoinRequestPayload(playerId, playerName)))
                _events.tryEmit(BluetoothEvent.ConnectedToHost(hostDisplayName))
            } catch (e: IOException) {
                _events.tryEmit(
                    BluetoothEvent.Error(
                        "اتصال به میزبان برقرار نشد. مطمئن شوید میزبان اتاق را باز کرده و هر دو دستگاه جفت شده‌اند."
                    )
                )
            }
        }
    }

    fun sendToHost(jsonLine: String) {
        val session = clientSession ?: return
        scope.launch {
            session.sendLine(jsonLine)
        }
    }

    fun stopAll() {
        _isHosting.value = false
        stopDiscovery()
        acceptJob?.cancel()
        acceptJob = null
        runCatching { serverSocket?.close() }
        serverSocket = null

        hostConnections.values.forEach { it.close() }
        hostConnections.clear()

        clientConnectJob?.cancel()
        clientConnectJob = null
        clientSession?.close()
        clientSession = null
    }

    fun release() {
        stopAll()
        if (discoveryReceiverRegistered) {
            runCatching { context.unregisterReceiver(discoveryReceiver) }
            discoveryReceiverRegistered = false
        }
    }

    private fun registerReceiverIfNeeded() {
        if (!discoveryReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(discoveryReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                context.registerReceiver(discoveryReceiver, filter)
            }
            discoveryReceiverRegistered = true
        }
    }

    private class SocketSession(
        val id: String,
        private val socket: BluetoothSocket,
        private val onMessage: (String) -> Unit,
        private val onClosed: () -> Unit
    ) {
        private val writeMutex = Mutex()
        private val reader = BufferedReader(InputStreamReader(socket.inputStream, Charsets.UTF_8))
        private val writer = BufferedWriter(OutputStreamWriter(socket.outputStream, Charsets.UTF_8))

        @Volatile
        private var closed = false

        fun startReading(scope: CoroutineScope) {
            scope.launch {
                try {
                    while (!closed) {
                        val line = reader.readLine() ?: break
                        if (line.isNotBlank()) {
                            onMessage(line)
                        }
                    }
                } catch (_: IOException) {
                } finally {
                    close()
                    onClosed()
                }
            }
        }

        suspend fun sendLine(line: String) {
            if (closed) return
            writeMutex.withLock {
                if (closed) return
                try {
                    writer.write(line)
                    writer.newLine()
                    writer.flush()
                } catch (_: IOException) {
                    close()
                    onClosed()
                }
            }
        }

        fun close() {
            if (closed) return
            closed = true
            runCatching { reader.close() }
            runCatching { writer.close() }
            runCatching { socket.close() }
        }
    }
}
