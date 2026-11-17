package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PlayerPreferencesRepository
import com.example.data.PlayerProfile
import com.example.game.Card
import com.example.game.ClientGameSnapshot
import com.example.game.GamePhase
import com.example.game.HokmBotAi
import com.example.game.HokmEngine
import com.example.game.QuickChatBanner
import com.example.game.Rank
import com.example.game.SeatPlayer
import com.example.game.Suit
import com.example.network.BluetoothEvent
import com.example.network.BluetoothGroupManager
import com.example.network.DiscoveredBluetoothDevice
import com.example.network.HokmJsonCodec
import com.example.network.MessageType
import com.example.network.PlayCardPayload
import com.example.network.QuickChatPayload
import com.example.network.RoomErrorPayload
import com.example.network.ChooseTrumpPayload
import com.example.ui.theme.TableTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SessionRole {
    NONE,
    SOLO,
    HOST,
    CLIENT
}

data class HokmUiState(
    val profile: PlayerProfile = PlayerProfile(
        playerId = "",
        playerName = "بازیکن حکم",
        preferredTargetHands = 7,
        hapticsEnabled = true,
        tableThemeId = TableTheme.ROYAL_EMERALD.id,
        matchesPlayed = 0,
        matchesWon = 0,
        kotsScored = 0
    ),
    val role: SessionRole = SessionRole.NONE,
    val isBluetoothSupported: Boolean = false,
    val isBluetoothEnabled: Boolean = false,
    val hasBluetoothPermissions: Boolean = false,
    val pairedDevices: List<DiscoveredBluetoothDevice> = emptyList(),
    val discoveredDevices: List<DiscoveredBluetoothDevice> = emptyList(),
    val isScanning: Boolean = false,
    val isHosting: Boolean = false,
    val isConnectingToHost: Boolean = false,
    val connectedHostName: String? = null,
    val selectedSwapSeat: Int? = null,
    val snapshot: ClientGameSnapshot? = null,
    val selectedCardId: String? = null,
    val activeQuickChats: Map<Int, QuickChatBanner> = emptyMap(),
    val errorBanner: String? = null,
    val statusNotice: String? = null,
    val navigateToGameSignal: Long = 0L
)

class HokmViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = PlayerPreferencesRepository(application)
    val bluetoothManager = BluetoothGroupManager(application)
    private val engine = HokmEngine()

    private val _uiState = MutableStateFlow(HokmUiState())
    val uiState: StateFlow<HokmUiState> = _uiState.asStateFlow()

    private val clientSeatByAddress = mutableMapOf<String, Int>()
    private var automationJob: Job? = null
    private var matchResultRecordedForHand = false
    private var kotsInCurrentMatchByMyTeam = 0
    private var lastCountedHandNumber = 0

    init {
        viewModelScope.launch {
            prefsRepo.ensurePlayerId()
            prefsRepo.profileFlow.collect { profile ->
                _uiState.update { state ->
                    state.copy(profile = profile)
                }
                if (engine.phase == GamePhase.LOBBY && _uiState.value.role == SessionRole.HOST) {
                    val updated = engine.players.toMutableList()
                    updated[0] = updated[0].copy(
                        playerId = profile.playerId,
                        name = profile.playerName
                    )
                    engine.updateLobbyState(updated, engine.targetHands)
                    broadcastHostSnapshots()
                }
            }
        }

        viewModelScope.launch {
            bluetoothManager.pairedDevices.collect { paired ->
                _uiState.update { it.copy(pairedDevices = paired) }
            }
        }

        viewModelScope.launch {
            bluetoothManager.discoveredDevices.collect { found ->
                _uiState.update { it.copy(discoveredDevices = found) }
            }
        }

        viewModelScope.launch {
            bluetoothManager.isScanning.collect { scanning ->
                _uiState.update { it.copy(isScanning = scanning) }
            }
        }

        viewModelScope.launch {
            bluetoothManager.isHosting.collect { hosting ->
                _uiState.update { it.copy(isHosting = hosting) }
            }
        }

        viewModelScope.launch {
            bluetoothManager.events.collect { event ->
                handleBluetoothEvent(event)
            }
        }

        refreshBluetoothStatus()
    }

    fun refreshBluetoothStatus() {
        val supported = bluetoothManager.isBluetoothSupported()
        val enabled = bluetoothManager.isBluetoothEnabled()
        val hasPerms = bluetoothManager.hasAllPermissions()
        if (enabled && hasPerms) {
            bluetoothManager.refreshPairedDevices()
        }
        _uiState.update {
            it.copy(
                isBluetoothSupported = supported,
                isBluetoothEnabled = enabled,
                hasBluetoothPermissions = hasPerms
            )
        }
    }

    fun requiredBluetoothPermissions(): Array<String> = bluetoothManager.requiredPermissions()

    fun clearError() {
        _uiState.update { it.copy(errorBanner = null) }
    }

    fun clearNotice() {
        _uiState.update { it.copy(statusNotice = null) }
    }

    fun updatePlayerName(newName: String) {
        viewModelScope.launch {
            prefsRepo.updatePlayerName(newName)
        }
    }

    fun updatePreferredTargetHands(target: Int) {
        viewModelScope.launch {
            prefsRepo.updatePreferredTargetHands(target)
        }
        if (_uiState.value.role == SessionRole.HOST || _uiState.value.role == SessionRole.SOLO) {
            engine.updateLobbyState(engine.players, target)
            broadcastHostSnapshots()
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setHapticsEnabled(enabled)
        }
    }

    fun updateTableTheme(theme: TableTheme) {
        viewModelScope.launch {
            prefsRepo.updateTableTheme(theme.id)
        }
    }

    fun startSoloMatch() {
        automationJob?.cancel()
        bluetoothManager.stopAll()
        clientSeatByAddress.clear()

        val profile = _uiState.value.profile
        val players = HokmEngine.defaultLobbyPlayers(profile.playerName).mapIndexed { index, seat ->
            if (index == 0) seat.copy(playerId = profile.playerId, name = profile.playerName)
            else seat
        }
        resetMatchTracking()
        engine.startNewMatch(players, profile.preferredTargetHands)
        _uiState.update {
            it.copy(
                role = SessionRole.SOLO,
                selectedCardId = null,
                selectedSwapSeat = null,
                errorBanner = null,
                snapshot = engine.snapshotForSeat(0),
                navigateToGameSignal = System.currentTimeMillis()
            )
        }
        scheduleNextHostStep()
    }

    fun enterHostLobby() {
        automationJob?.cancel()
        bluetoothManager.stopAll()
        clientSeatByAddress.clear()
        refreshBluetoothStatus()

        val profile = _uiState.value.profile
        val initialPlayers = HokmEngine.defaultLobbyPlayers(profile.playerName).mapIndexed { index, seat ->
            if (index == 0) seat.copy(playerId = profile.playerId, name = profile.playerName)
            else seat
        }
        engine.resetToLobby()
        engine.updateLobbyState(initialPlayers, profile.preferredTargetHands)

        _uiState.update {
            it.copy(
                role = SessionRole.HOST,
                selectedSwapSeat = null,
                selectedCardId = null,
                errorBanner = null,
                connectedHostName = null,
                snapshot = engine.snapshotForSeat(0)
            )
        }

        if (bluetoothManager.isBluetoothSupported() &&
            bluetoothManager.isBluetoothEnabled() &&
            bluetoothManager.hasConnectPermission()
        ) {
            bluetoothManager.startHosting()
        }
    }

    fun restartBluetoothHostServer() {
        refreshBluetoothStatus()
        if (bluetoothManager.isBluetoothEnabled() && bluetoothManager.hasConnectPermission()) {
            bluetoothManager.startHosting()
        }
    }

    fun enterJoinLobby() {
        automationJob?.cancel()
        bluetoothManager.stopAll()
        clientSeatByAddress.clear()
        refreshBluetoothStatus()
        _uiState.update {
            it.copy(
                role = SessionRole.CLIENT,
                isConnectingToHost = false,
                connectedHostName = null,
                selectedCardId = null,
                errorBanner = null,
                snapshot = null
            )
        }
    }

    fun startBluetoothScan() {
        refreshBluetoothStatus()
        bluetoothManager.startDiscovery()
    }

    fun stopBluetoothScan() {
        bluetoothManager.stopDiscovery()
    }

    fun connectToHost(device: DiscoveredBluetoothDevice) {
        val profile = _uiState.value.profile
        _uiState.update {
            it.copy(
                role = SessionRole.CLIENT,
                isConnectingToHost = true,
                errorBanner = null
            )
        }
        bluetoothManager.connectToHost(
            deviceAddress = device.address,
            playerId = profile.playerId,
            playerName = profile.playerName
        )
    }

    fun onLobbySeatClicked(seatIndex: Int) {
        if (_uiState.value.role != SessionRole.HOST || engine.phase != GamePhase.LOBBY) return
        val firstSelected = _uiState.value.selectedSwapSeat
        if (firstSelected == null) {
            _uiState.update { it.copy(selectedSwapSeat = seatIndex) }
        } else if (firstSelected == seatIndex) {
            _uiState.update { it.copy(selectedSwapSeat = null) }
        } else {
            swapLobbySeats(firstSelected, seatIndex)
            _uiState.update { it.copy(selectedSwapSeat = null) }
        }
    }

    private fun swapLobbySeats(seatA: Int, seatB: Int) {
        val list = engine.players.toMutableList()
        val playerA = list[seatA]
        val playerB = list[seatB]
        list[seatA] = playerB.copy(seatIndex = seatA)
        list[seatB] = playerA.copy(seatIndex = seatB)

        // Update address -> seat mappings
        val updatedMap = clientSeatByAddress.toMutableMap()
        for ((addr, seat) in clientSeatByAddress) {
            if (seat == seatA) updatedMap[addr] = seatB
            else if (seat == seatB) updatedMap[addr] = seatA
        }
        clientSeatByAddress.clear()
        clientSeatByAddress.putAll(updatedMap)

        engine.updateLobbyState(list, engine.targetHands)
        broadcastHostSnapshots()
    }

    fun startHostedMatch() {
        if (_uiState.value.role != SessionRole.HOST) return
        resetMatchTracking()
        engine.startNewMatch(engine.players, engine.targetHands)
        broadcastHostSnapshots()
        _uiState.update {
            it.copy(
                selectedCardId = null,
                navigateToGameSignal = System.currentTimeMillis()
            )
        }
        scheduleNextHostStep()
    }

    fun chooseTrump(suit: Suit) {
        val snap = _uiState.value.snapshot ?: return
        if (snap.phase != GamePhase.CHOOSING_TRUMP || snap.hakemSeat != snap.mySeat) return

        when (_uiState.value.role) {
            SessionRole.HOST, SessionRole.SOLO -> {
                if (engine.chooseTrump(snap.mySeat, suit)) {
                    broadcastHostSnapshots()
                    scheduleNextHostStep()
                }
            }
            SessionRole.CLIENT -> {
                bluetoothManager.sendToHost(HokmJsonCodec.encodeChooseTrump(ChooseTrumpPayload(suit.name)))
            }
            SessionRole.NONE -> Unit
        }
    }

    fun onCardTapped(card: Card) {
        val snap = _uiState.value.snapshot ?: return
        if (snap.phase != GamePhase.PLAYING_TRICK || snap.currentTurnSeat != snap.mySeat) return

        val legal = legalCardsFromSnapshot(snap)
        if (card !in legal) {
            _uiState.update {
                it.copy(statusNotice = "طبق قانون باید از خال زمینه (${snap.currentTrick.firstOrNull()?.card?.suit?.persianName ?: ""}) بازی کنید.")
            }
            return
        }

        if (_uiState.value.selectedCardId == card.id) {
            _uiState.update { it.copy(selectedCardId = null) }
            playCardInternal(card)
        } else {
            _uiState.update { it.copy(selectedCardId = card.id) }
        }
    }

    fun playSelectedCardNow() {
        val snap = _uiState.value.snapshot ?: return
        val selectedId = _uiState.value.selectedCardId ?: return
        val card = snap.myHand.firstOrNull { it.id == selectedId } ?: return
        _uiState.update { it.copy(selectedCardId = null) }
        playCardInternal(card)
    }

    private fun playCardInternal(card: Card) {
        val snap = _uiState.value.snapshot ?: return
        when (_uiState.value.role) {
            SessionRole.HOST, SessionRole.SOLO -> {
                if (engine.playCard(snap.mySeat, card)) {
                    broadcastHostSnapshots()
                    scheduleNextHostStep()
                }
            }
            SessionRole.CLIENT -> {
                bluetoothManager.sendToHost(
                    HokmJsonCodec.encodePlayCard(PlayCardPayload(card.suit.name, card.rank.name))
                )
            }
            SessionRole.NONE -> Unit
        }
    }

    fun startNextHand() {
        if (_uiState.value.role == SessionRole.CLIENT) return
        engine.startNextHand()
        _uiState.update { it.copy(selectedCardId = null) }
        broadcastHostSnapshots()
        scheduleNextHostStep()
    }

    fun rematch() {
        if (_uiState.value.role == SessionRole.CLIENT) return
        resetMatchTracking()
        engine.startNewMatch(engine.players, engine.targetHands)
        _uiState.update { it.copy(selectedCardId = null) }
        broadcastHostSnapshots()
        scheduleNextHostStep()
    }

    fun sendQuickChat(message: String) {
        val snap = _uiState.value.snapshot ?: return
        val myPlayer = snap.players.getOrNull(snap.mySeat) ?: return
        val payload = QuickChatPayload(
            seatIndex = snap.mySeat,
            senderName = myPlayer.name,
            message = message
        )
        when (_uiState.value.role) {
            SessionRole.SOLO -> showQuickChatLocally(payload)
            SessionRole.HOST -> {
                showQuickChatLocally(payload)
                val encoded = HokmJsonCodec.encodeQuickChat(payload)
                clientSeatByAddress.keys.forEach { addr ->
                    bluetoothManager.sendToClient(addr, encoded)
                }
            }
            SessionRole.CLIENT -> {
                bluetoothManager.sendToHost(HokmJsonCodec.encodeQuickChat(payload))
            }
            SessionRole.NONE -> Unit
        }
    }

    fun leaveSession() {
        automationJob?.cancel()
        bluetoothManager.stopAll()
        clientSeatByAddress.clear()
        engine.resetToLobby()
        _uiState.update {
            it.copy(
                role = SessionRole.NONE,
                isConnectingToHost = false,
                connectedHostName = null,
                selectedSwapSeat = null,
                selectedCardId = null,
                snapshot = null,
                activeQuickChats = emptyMap()
            )
        }
    }

    fun legalCardsFromSnapshot(snap: ClientGameSnapshot): Set<Card> {
        if (snap.phase != GamePhase.PLAYING_TRICK || snap.currentTurnSeat != snap.mySeat) {
            return emptySet()
        }
        val lead = snap.currentTrick.firstOrNull()?.card?.suit ?: return snap.myHand.toSet()
        val matching = snap.myHand.filter { it.suit == lead }
        return (matching.ifEmpty { snap.myHand }).toSet()
    }

    private fun handleBluetoothEvent(event: BluetoothEvent) {
        when (event) {
            is BluetoothEvent.ClientConnected -> {
                _uiState.update { it.copy(statusNotice = "${event.deviceName} متصل شد") }
            }
            is BluetoothEvent.ClientMessage -> {
                handleClientMessageOnHost(event.clientId, event.envelope)
            }
            is BluetoothEvent.ClientDisconnected -> {
                val seat = clientSeatByAddress.remove(event.clientId)
                if (seat != null) {
                    val oldName = engine.players[seat].name
                    if (engine.phase == GamePhase.LOBBY) {
                        val defaultBotName = when (seat) {
                            1 -> "ربات سهراب"
                            2 -> "ربات رستم"
                            else -> "ربات کاوه"
                        }
                        engine.updatePlayerConnection(
                            seat,
                            SeatPlayer(seat, "bot_$seat", defaultBotName, isBot = true, isConnected = true, isHost = false)
                        )
                    } else {
                        engine.replaceSeatWithBot(seat)
                    }
                    _uiState.update {
                        it.copy(statusNotice = "$oldName از بازی خارج شد و ربات جایگزین شد.")
                    }
                    broadcastHostSnapshots()
                    scheduleNextHostStep()
                }
            }
            is BluetoothEvent.ConnectedToHost -> {
                _uiState.update {
                    it.copy(
                        isConnectingToHost = false,
                        connectedHostName = event.hostName,
                        statusNotice = "به میز ${event.hostName} متصل شدید!"
                    )
                }
            }
            is BluetoothEvent.HostMessage -> {
                handleHostMessageOnClient(event.envelope)
            }
            is BluetoothEvent.DisconnectedFromHost -> {
                _uiState.update {
                    it.copy(
                        isConnectingToHost = false,
                        connectedHostName = null,
                        errorBanner = event.reason
                    )
                }
            }
            is BluetoothEvent.Error -> {
                _uiState.update {
                    it.copy(
                        isConnectingToHost = false,
                        errorBanner = event.message
                    )
                }
            }
        }
    }

    private fun handleClientMessageOnHost(clientId: String, envelope: com.example.network.NetworkEnvelope) {
        if (_uiState.value.role != SessionRole.HOST) return
        when (envelope.type) {
            MessageType.JOIN_REQUEST -> {
                val req = HokmJsonCodec.decodeJoin(envelope.payload) ?: return
                // Reconnect if same playerId already seated
                val existingSeat = engine.players.indexOfFirst { !it.isHost && it.playerId == req.playerId }
                if (existingSeat >= 0) {
                    clientSeatByAddress[clientId] = existingSeat
                    engine.updatePlayerConnection(
                        existingSeat,
                        engine.players[existingSeat].copy(
                            name = req.playerName,
                            isBot = false,
                            isConnected = true
                        )
                    )
                    broadcastHostSnapshots()
                    return
                }

                if (engine.phase != GamePhase.LOBBY) {
                    bluetoothManager.sendToClient(
                        clientId,
                        HokmJsonCodec.encodeError(RoomErrorPayload("بازی در حال انجام است و امکان ورود جدید وجود ندارد."))
                    )
                    bluetoothManager.disconnectClient(clientId)
                    return
                }

                // Assign first available bot seat in preferred order: 2 (partner), 1, 3
                val seatOrder = listOf(1, 2, 3)
                val openSeat = seatOrder.firstOrNull { seat -> engine.players[seat].isBot }
                if (openSeat == null) {
                    bluetoothManager.sendToClient(
                        clientId,
                        HokmJsonCodec.encodeError(RoomErrorPayload("ظرفیت میز ۴ نفره تکمیل است."))
                    )
                    bluetoothManager.disconnectClient(clientId)
                    return
                }

                clientSeatByAddress[clientId] = openSeat
                engine.updatePlayerConnection(
                    openSeat,
                    SeatPlayer(
                        seatIndex = openSeat,
                        playerId = req.playerId,
                        name = req.playerName.take(18).ifBlank { "بازیکن" },
                        isBot = false,
                        isConnected = true,
                        isHost = false
                    )
                )
                broadcastHostSnapshots()
            }
            MessageType.CHOOSE_TRUMP -> {
                val seat = clientSeatByAddress[clientId] ?: return
                val payload = HokmJsonCodec.decodeChooseTrump(envelope.payload) ?: return
                val suit = runCatching { Suit.valueOf(payload.suitName) }.getOrNull() ?: return
                if (engine.chooseTrump(seat, suit)) {
                    broadcastHostSnapshots()
                    scheduleNextHostStep()
                }
            }
            MessageType.PLAY_CARD -> {
                val seat = clientSeatByAddress[clientId] ?: return
                val payload = HokmJsonCodec.decodePlayCard(envelope.payload) ?: return
                val suit = runCatching { Suit.valueOf(payload.suitName) }.getOrNull() ?: return
                val rank = runCatching { Rank.valueOf(payload.rankName) }.getOrNull() ?: return
                if (engine.playCard(seat, Card(suit, rank))) {
                    broadcastHostSnapshots()
                    scheduleNextHostStep()
                }
            }
            MessageType.QUICK_CHAT -> {
                val payload = HokmJsonCodec.decodeQuickChat(envelope.payload) ?: return
                val actualSeat = clientSeatByAddress[clientId] ?: payload.seatIndex
                val verified = payload.copy(
                    seatIndex = actualSeat,
                    senderName = engine.players[actualSeat].name
                )
                showQuickChatLocally(verified)
                val encoded = HokmJsonCodec.encodeQuickChat(verified)
                clientSeatByAddress.keys.forEach { addr ->
                    bluetoothManager.sendToClient(addr, encoded)
                }
            }
        }
    }

    private fun handleHostMessageOnClient(envelope: com.example.network.NetworkEnvelope) {
        when (envelope.type) {
            MessageType.SNAPSHOT -> {
                val snap = HokmJsonCodec.decodeSnapshot(envelope.payload) ?: return
                val prevPhase = _uiState.value.snapshot?.phase
                val shouldEnterGame = (prevPhase == null || prevPhase == GamePhase.LOBBY) &&
                    snap.phase != GamePhase.LOBBY
                recordStatsIfNeeded(snap)
                _uiState.update { state ->
                    state.copy(
                        snapshot = snap,
                        selectedCardId = if (snap.currentTurnSeat != snap.mySeat) null else state.selectedCardId,
                        navigateToGameSignal = if (shouldEnterGame) System.currentTimeMillis() else state.navigateToGameSignal
                    )
                }
            }
            MessageType.QUICK_CHAT -> {
                val payload = HokmJsonCodec.decodeQuickChat(envelope.payload) ?: return
                showQuickChatLocally(payload)
            }
            MessageType.ROOM_ERROR -> {
                val err = HokmJsonCodec.decodeError(envelope.payload) ?: return
                _uiState.update { it.copy(errorBanner = err.message, isConnectingToHost = false) }
            }
        }
    }

    private fun broadcastHostSnapshots() {
        val hostSeat = engine.players.indexOfFirst { it.isHost }.takeIf { it >= 0 } ?: 0
        val hostSnap = engine.snapshotForSeat(hostSeat)
        recordStatsIfNeeded(hostSnap)
        _uiState.update { it.copy(snapshot = hostSnap) }

        if (_uiState.value.role == SessionRole.HOST) {
            for ((clientAddr, seatIndex) in clientSeatByAddress) {
                val clientSnap = engine.snapshotForSeat(seatIndex)
                bluetoothManager.sendToClient(clientAddr, HokmJsonCodec.encodeSnapshot(clientSnap))
            }
        }
    }

    private fun scheduleNextHostStep() {
        if (_uiState.value.role == SessionRole.CLIENT || _uiState.value.role == SessionRole.NONE) return
        automationJob?.cancel()
        automationJob = viewModelScope.launch {
            while (true) {
                when (engine.phase) {
                    GamePhase.TRICK_PAUSE -> {
                        delay(1350L)
                        engine.advanceAfterTrickPause()
                        broadcastHostSnapshots()
                    }
                    GamePhase.CHOOSING_TRUMP -> {
                        val hakem = engine.players[engine.hakemSeat]
                        if (!hakem.isBot) break
                        delay(950L)
                        val chosen = HokmBotAi.chooseTrump(engine.handFor(engine.hakemSeat))
                        engine.chooseTrump(engine.hakemSeat, chosen)
                        broadcastHostSnapshots()
                    }
                    GamePhase.PLAYING_TRICK -> {
                        val turnSeat = engine.currentTurnSeat
                        val player = engine.players[turnSeat]
                        if (!player.isBot) break
                        delay(760L)
                        val trump = engine.trumpSuit ?: Suit.SPADES
                        val hand = engine.handFor(turnSeat)
                        val legal = engine.legalCardsFor(turnSeat)
                        val chosenCard = HokmBotAi.chooseCard(
                            hand = hand,
                            legalCards = legal,
                            currentTrick = engine.currentTrick,
                            trumpSuit = trump,
                            mySeat = turnSeat
                        )
                        engine.playCard(turnSeat, chosenCard)
                        broadcastHostSnapshots()
                    }
                    else -> break
                }
            }
        }
    }

    private fun showQuickChatLocally(payload: QuickChatPayload) {
        val bannerId = System.currentTimeMillis()
        val banner = QuickChatBanner(
            id = bannerId,
            seatIndex = payload.seatIndex,
            senderName = payload.senderName,
            message = payload.message
        )
        _uiState.update { state ->
            state.copy(activeQuickChats = state.activeQuickChats + (payload.seatIndex to banner))
        }
        viewModelScope.launch {
            delay(2800L)
            _uiState.update { state ->
                val current = state.activeQuickChats[payload.seatIndex]
                if (current?.id == bannerId) {
                    state.copy(activeQuickChats = state.activeQuickChats - payload.seatIndex)
                } else {
                    state
                }
            }
        }
    }

    private fun resetMatchTracking() {
        matchResultRecordedForHand = false
        kotsInCurrentMatchByMyTeam = 0
        lastCountedHandNumber = 0
    }

    private fun recordStatsIfNeeded(snap: ClientGameSnapshot) {
        val myTeam = if (snap.mySeat % 2 == 0) 1 else 2
        val summary = snap.lastHandSummary
        if (summary != null && snap.handNumber > lastCountedHandNumber &&
            (snap.phase == GamePhase.HAND_OVER || snap.phase == GamePhase.MATCH_OVER)
        ) {
            lastCountedHandNumber = snap.handNumber
            if (summary.isKot && summary.winningTeam == myTeam) {
                kotsInCurrentMatchByMyTeam += 1
            }
        }

        if (snap.phase == GamePhase.MATCH_OVER && !matchResultRecordedForHand) {
            matchResultRecordedForHand = true
            val myScore = if (myTeam == 1) snap.team1MatchScore else snap.team2MatchScore
            val oppScore = if (myTeam == 1) snap.team2MatchScore else snap.team1MatchScore
            val didWin = myScore > oppScore
            viewModelScope.launch {
                prefsRepo.recordMatchResult(didWin = didWin, kotsInMatch = kotsInCurrentMatchByMyTeam)
            }
        } else if (snap.phase == GamePhase.CHOOSING_TRUMP && snap.handNumber == 1) {
            matchResultRecordedForHand = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        automationJob?.cancel()
        bluetoothManager.release()
    }
}
