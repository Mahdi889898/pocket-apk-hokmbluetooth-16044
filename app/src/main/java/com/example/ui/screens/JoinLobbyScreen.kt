package com.example.ui.screens

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.BluetoothSearching
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.network.DiscoveredBluetoothDevice
import com.example.ui.components.FeltTableBackground
import com.example.ui.theme.BluetoothBlue
import com.example.ui.theme.BrassGold
import com.example.ui.theme.BrassGoldBright
import com.example.ui.theme.EmeraldFeltDeep
import com.example.ui.theme.EmeraldFeltMid
import com.example.ui.theme.IvoryText
import com.example.ui.theme.JadeSuccess
import com.example.ui.theme.RubyDanger
import com.example.ui.theme.TableTheme
import com.example.ui.theme.WarmMutedText
import com.example.viewmodel.HokmUiState

@Composable
fun JoinLobbyScreen(
    state: HokmUiState,
    requiredPermissions: Array<String>,
    onRefreshBluetooth: () -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnectDevice: (DiscoveredBluetoothDevice) -> Unit,
    onPlaySoloFallback: () -> Unit,
    onDismissError: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        onRefreshBluetooth()
    }

    val enableBtLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        onRefreshBluetooth()
    }

    val tableTheme = TableTheme.fromId(state.profile.tableThemeId)

    FeltTableBackground(tableTheme = tableTheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("join_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = "بازگشت",
                            tint = BrassGoldBright
                        )
                    }
                    Column {
                        Text(
                            text = "اتصال به میزبان (بلوتوث)",
                            style = MaterialTheme.typography.titleLarge,
                            color = IvoryText,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (state.connectedHostName != null) {
                                "متصل به میز ${state.connectedHostName}"
                            } else {
                                "گوشی میزبان را از لیست زیر انتخاب کنید"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (state.connectedHostName != null) JadeSuccess else WarmMutedText
                        )
                    }
                }

                state.errorBanner?.let { error ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = RubyDanger.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, RubyDanger),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onDismissError)
                    ) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = IvoryText,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Connected Waiting Lobby View
                val lobbySnap = state.snapshot
                if (state.connectedHostName != null && lobbySnap != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = EmeraldFeltMid.copy(alpha = 0.9f),
                        border = BorderStroke(1.2.dp, JadeSuccess.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = JadeSuccess,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "به میز «${state.connectedHostName}» وارد شدید!",
                                style = MaterialTheme.typography.titleMedium,
                                color = IvoryText,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "صندلی شما: شماره ${lobbySnap.mySeat + 1} (${if (lobbySnap.mySeat % 2 == 0) "تیم ۱" else "تیم ۲"}) • بازی ${lobbySnap.targetHands} دستی",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BrassGoldBright
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                lobbySnap.players.forEach { seatPlayer ->
                                    val isMe = seatPlayer.seatIndex == lobbySnap.mySeat
                                    val label = when {
                                        isMe -> "شما • تیم ${seatPlayer.teamIndex}"
                                        seatPlayer.teamIndex == (if (lobbySnap.mySeat % 2 == 0) 1 else 2) -> "هم‌تیمی شما • تیم ${seatPlayer.teamIndex}"
                                        else -> "حریف • تیم ${seatPlayer.teamIndex}"
                                    }
                                    LobbySeatPill(
                                        player = seatPlayer,
                                        teamLabel = label,
                                        isSelected = isMe,
                                        onClick = null,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(50)),
                                color = BrassGold,
                                trackColor = EmeraldFeltDeep
                            )
                            Text(
                                text = "منتظر شروع بازی توسط میزبان...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = WarmMutedText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    BluetoothSetupCard(
                        isSupported = state.isBluetoothSupported,
                        hasPermissions = state.hasBluetoothPermissions,
                        isEnabled = state.isBluetoothEnabled,
                        isHostMode = false,
                        onRequestPermissions = { permissionLauncher.launch(requiredPermissions) },
                        onEnableBluetooth = {
                            enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                        },
                        onMakeDiscoverable = {},
                        onPlaySoloFallback = onPlaySoloFallback
                    )

                    if (state.isBluetoothSupported && state.hasBluetoothPermissions && state.isBluetoothEnabled) {
                        Button(
                            onClick = { if (state.isScanning) onStopScan() else onStartScan() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("scan_bluetooth_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BluetoothBlue,
                                contentColor = EmeraldFeltDeep
                            )
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Rounded.BluetoothSearching, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = if (state.isScanning) "توقف اسکن بلوتوث" else "اسکن دستگاه‌های نزدیک",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (state.isScanning || state.isConnectingToHost) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = BrassGoldBright
                                )
                                Text(
                                    text = if (state.isConnectingToHost) "در حال برقراری اتصال بلوتوثی..." else "در حال جستجوی گوشی‌های اطراف...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = BrassGoldBright
                                )
                            }
                        }

                        // Nearby Discovered Devices
                        if (state.discoveredDevices.isNotEmpty()) {
                            Text(
                                text = "دستگاه‌های یافت‌شده در اطراف",
                                style = MaterialTheme.typography.titleSmall,
                                color = BrassGoldBright
                            )
                            state.discoveredDevices.forEach { device ->
                                BluetoothDeviceRow(
                                    device = device,
                                    enabled = !state.isConnectingToHost,
                                    onClick = { onConnectDevice(device) }
                                )
                            }
                        }

                        // Paired Devices
                        Text(
                            text = "دستگاه‌های جفت‌شده (Paired)",
                            style = MaterialTheme.typography.titleSmall,
                            color = IvoryText
                        )
                        if (state.pairedDevices.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = EmeraldFeltMid.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "هیچ دستگاه جفت‌شده‌ای یافت نشد. دکمه اسکن را بزنید یا گوشی میزبان را در تنظیمات بلوتوث جفت کنید.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = WarmMutedText,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        } else {
                            state.pairedDevices.forEach { device ->
                                BluetoothDeviceRow(
                                    device = device,
                                    enabled = !state.isConnectingToHost,
                                    onClick = { onConnectDevice(device) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BluetoothDeviceRow(
    device: DiscoveredBluetoothDevice,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .testTag("bt_device_${device.address}"),
        shape = RoundedCornerShape(16.dp),
        color = EmeraldFeltMid.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, BluetoothBlue.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = BluetoothBlue.copy(alpha = 0.18f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Bluetooth,
                            contentDescription = null,
                            tint = BluetoothBlue
                        )
                    }
                }
                Column {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = IvoryText,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (device.isPaired) "جفت‌شده • ${device.address}" else device.address,
                        style = MaterialTheme.typography.labelSmall,
                        color = WarmMutedText
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Link,
                    contentDescription = "اتصال",
                    tint = BrassGoldBright,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "اتصال",
                    style = MaterialTheme.typography.labelLarge,
                    color = BrassGoldBright
                )
            }
        }
    }
}
