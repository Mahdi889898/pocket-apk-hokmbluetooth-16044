package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.BluetoothConnected
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.game.Card
import com.example.game.ClientGameSnapshot
import com.example.game.GamePhase
import com.example.game.QuickChatBanner
import com.example.game.SeatPlayer
import com.example.game.Suit
import com.example.ui.components.FeltTableBackground
import com.example.ui.components.PlayingCardBack
import com.example.ui.components.PlayingCardFront
import com.example.ui.components.TableThemePickerRow
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BluetoothBlue
import com.example.ui.theme.BrassGold
import com.example.ui.theme.BrassGoldBright
import com.example.ui.theme.CrimsonSuit
import com.example.ui.theme.EmeraldFeltDeep
import com.example.ui.theme.IvoryCard
import com.example.ui.theme.IvoryText
import com.example.ui.theme.JadeSuccess
import com.example.ui.theme.ObsidianSuit
import com.example.ui.theme.RubyDanger
import com.example.ui.theme.TableTheme
import com.example.ui.theme.WarmMutedText
import com.example.viewmodel.HokmUiState
import com.example.viewmodel.SessionRole

private val QUICK_CHAT_PHRASES = listOf(
    "بزن!",
    "عالی بود!",
    "بُرش!",
    "دست‌خوش!",
    "خوش‌شانس!",
    "حواست باشه!"
)

@Composable
fun GameTableScreen(
    state: HokmUiState,
    legalCards: Set<Card>,
    onChooseTrump: (Suit) -> Unit,
    onCardTapped: (Card) -> Unit,
    onPlaySelectedCard: () -> Unit,
    onNextHand: () -> Unit,
    onRematch: () -> Unit,
    onSendQuickChat: (String) -> Unit,
    onSelectTableTheme: (TableTheme) -> Unit,
    onDismissNotice: () -> Unit,
    onExitMatch: () -> Unit
) {
    var showExitDialog by remember { mutableStateOf(false) }
    var showQuickChatBar by remember { mutableStateOf(false) }
    var showThemePickerDialog by remember { mutableStateOf(false) }

    BackHandler {
        showExitDialog = true
    }

    val snap = state.snapshot
    val haptic = LocalHapticFeedback.current
    val tableTheme = TableTheme.fromId(state.profile.tableThemeId)

    LaunchedEffect(snap?.currentTurnSeat, snap?.phase) {
        if (state.profile.hapticsEnabled &&
            snap != null &&
            snap.currentTurnSeat == snap.mySeat &&
            (snap.phase == GamePhase.PLAYING_TRICK || snap.phase == GamePhase.CHOOSING_TRUMP)
        ) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    FeltTableBackground(tableTheme = tableTheme) {
        if (snap == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "در حال همگام‌سازی میز بازی...",
                    style = MaterialTheme.typography.titleMedium,
                    color = IvoryText
                )
            }
        } else {
            val myTeam = if (snap.mySeat % 2 == 0) 1 else 2
            val myTricks = if (myTeam == 1) snap.team1Tricks else snap.team2Tricks
            val oppTricks = if (myTeam == 1) snap.team2Tricks else snap.team1Tricks
            val myMatchScore = if (myTeam == 1) snap.team1MatchScore else snap.team2MatchScore
            val oppMatchScore = if (myTeam == 1) snap.team2MatchScore else snap.team1MatchScore

            val topSeat = (snap.mySeat + 2) % 4
            val rightSeat = (snap.mySeat + 1) % 4
            val leftSeat = (snap.mySeat + 3) % 4
            val isMyTurn = snap.currentTurnSeat == snap.mySeat

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Luxury Scoreboard + Trump Medallion + Utility Controls
                LuxuryScoreboardHud(
                    snap = snap,
                    role = state.role,
                    tableTheme = tableTheme,
                    myMatchScore = myMatchScore,
                    oppMatchScore = oppMatchScore,
                    myTricks = myTricks,
                    oppTricks = oppTricks,
                    onOpenThemePicker = { showThemePickerDialog = true },
                    onToggleChat = { showQuickChatBar = !showQuickChatBar },
                    onRequestExit = { showExitDialog = true }
                )

                // Status notice pill
                AnimatedVisibility(visible = state.statusNotice != null || snap.statusBanner != null) {
                    val bannerText = state.statusNotice ?: snap.statusBanner ?: ""
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = tableTheme.deepColor.copy(alpha = 0.88f),
                            border = BorderStroke(1.dp, tableTheme.accentColor.copy(alpha = 0.65f)),
                            modifier = Modifier.clickable(onClick = onDismissNotice)
                        ) {
                            Text(
                                text = bannerText,
                                style = MaterialTheme.typography.labelMedium,
                                color = tableTheme.accentBright,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Main Center Table Felt (Top Partner, Left/Right Opponents, Center Trick)
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val tableWidth = maxWidth
                    val tableHeight = maxHeight
                    val trickCardW = if (tableWidth < 380.dp) 58.dp else 70.dp
                    val trickCardH = if (tableWidth < 380.dp) 86.dp else 102.dp

                    // Subtle central trick-platter ring
                    Canvas(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(if (tableWidth < 380.dp) 186.dp else 218.dp)
                    ) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    tableTheme.accentColor.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                        drawCircle(
                            color = tableTheme.accentColor.copy(alpha = 0.24f),
                            style = Stroke(width = 1.4.dp.toPx())
                        )
                    }

                    // Top Partner Seat
                    snap.players.getOrNull(topSeat)?.let { partner ->
                        TableSeatBadge(
                            player = partner,
                            roleLabel = "یار شما",
                            teamColor = JadeSuccess,
                            cardCount = snap.handCounts.getOrElse(topSeat) { 0 },
                            isHakem = snap.hakemSeat == topSeat,
                            isTurn = snap.currentTurnSeat == topSeat &&
                                (snap.phase == GamePhase.PLAYING_TRICK || snap.phase == GamePhase.CHOOSING_TRUMP),
                            tableTheme = tableTheme,
                            chatBanner = state.activeQuickChats[topSeat],
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 4.dp)
                        )
                    }

                    // Left Opponent Seat
                    snap.players.getOrNull(leftSeat)?.let { leftPlayer ->
                        TableSeatBadge(
                            player = leftPlayer,
                            roleLabel = "حریف",
                            teamColor = RubyDanger,
                            cardCount = snap.handCounts.getOrElse(leftSeat) { 0 },
                            isHakem = snap.hakemSeat == leftSeat,
                            isTurn = snap.currentTurnSeat == leftSeat &&
                                (snap.phase == GamePhase.PLAYING_TRICK || snap.phase == GamePhase.CHOOSING_TRUMP),
                            tableTheme = tableTheme,
                            chatBanner = state.activeQuickChats[leftSeat],
                            compact = true,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 2.dp)
                        )
                    }

                    // Right Opponent Seat
                    snap.players.getOrNull(rightSeat)?.let { rightPlayer ->
                        TableSeatBadge(
                            player = rightPlayer,
                            roleLabel = "حریف",
                            teamColor = RubyDanger,
                            cardCount = snap.handCounts.getOrElse(rightSeat) { 0 },
                            isHakem = snap.hakemSeat == rightSeat,
                            isTurn = snap.currentTurnSeat == rightSeat &&
                                (snap.phase == GamePhase.PLAYING_TRICK || snap.phase == GamePhase.CHOOSING_TRUMP),
                            tableTheme = tableTheme,
                            chatBanner = state.activeQuickChats[rightSeat],
                            compact = true,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 2.dp)
                        )
                    }

                    // Center Trick Cards (4 slots relative to mySeat)
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(
                                width = (tableWidth * 0.58f).coerceAtLeast(186.dp),
                                height = (tableHeight * 0.56f).coerceAtLeast(176.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val offsetX = if (tableWidth < 380.dp) 44.dp else 54.dp
                        val offsetY = if (tableHeight < 360.dp) 36.dp else 46.dp

                        snap.currentTrick.forEach { played ->
                            val isWinning = snap.phase == GamePhase.TRICK_PAUSE &&
                                snap.lastTrickWinnerSeat == played.seatIndex
                            val (dx, dy) = when (played.seatIndex) {
                                snap.mySeat -> 0.dp to offsetY
                                topSeat -> 0.dp to (-offsetY)
                                rightSeat -> offsetX to 0.dp
                                leftSeat -> (-offsetX) to 0.dp
                                else -> 0.dp to 0.dp
                            }
                            PlayingCardFront(
                                card = played.card,
                                width = trickCardW,
                                height = trickCardH,
                                isTrump = played.card.suit == snap.trumpSuit,
                                isWinningCard = isWinning,
                                accentColor = tableTheme.accentColor,
                                modifier = Modifier.offset(x = dx, y = dy)
                            )
                        }
                    }

                    // Choosing Trump Overlay
                    if (snap.phase == GamePhase.CHOOSING_TRUMP) {
                        ChoosingTrumpOverlay(
                            isMyHakem = snap.hakemSeat == snap.mySeat,
                            hakemName = snap.players.getOrNull(snap.hakemSeat)?.name ?: "حاکم",
                            tableTheme = tableTheme,
                            onChooseTrump = onChooseTrump,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    // Hand Over Overlay
                    if (snap.phase == GamePhase.HAND_OVER && snap.lastHandSummary != null) {
                        HandOverOverlay(
                            snap = snap,
                            myTeam = myTeam,
                            tableTheme = tableTheme,
                            canControlNext = state.role != SessionRole.CLIENT,
                            onNextHand = onNextHand,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    // Match Over Overlay
                    if (snap.phase == GamePhase.MATCH_OVER) {
                        MatchOverOverlay(
                            snap = snap,
                            myTeam = myTeam,
                            tableTheme = tableTheme,
                            canRematch = state.role != SessionRole.CLIENT,
                            onRematch = onRematch,
                            onExit = onExitMatch,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                // Quick Chat Bar
                AnimatedVisibility(visible = showQuickChatBar) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(QUICK_CHAT_PHRASES) { phrase ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = tableTheme.midColor,
                                border = BorderStroke(1.dp, tableTheme.accentColor.copy(alpha = 0.55f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        onSendQuickChat(phrase)
                                        showQuickChatBar = false
                                    }
                            ) {
                                Text(
                                    text = phrase,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = IvoryText,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }

                // My Seat Status + Confirm Play Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val myPlayer = snap.players.getOrNull(snap.mySeat)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isMyTurn) tableTheme.accentColor else tableTheme.midColor,
                            border = BorderStroke(1.2.dp, tableTheme.accentBright.copy(alpha = 0.7f)),
                            shadowElevation = if (isMyTurn) 6.dp else 0.dp
                        ) {
                            Text(
                                text = buildString {
                                    append(myPlayer?.name ?: "شما")
                                    if (snap.hakemSeat == snap.mySeat) append(" • 👑 حاکم")
                                    if (isMyTurn && snap.phase == GamePhase.PLAYING_TRICK) append(" • نوبت شما!")
                                },
                                color = if (isMyTurn) tableTheme.deepColor else IvoryText,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }

                        state.activeQuickChats[snap.mySeat]?.let { myChat ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = tableTheme.accentBright
                            ) {
                                Text(
                                    text = myChat.message,
                                    color = tableTheme.deepColor,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    if (state.selectedCardId != null && isMyTurn && snap.phase == GamePhase.PLAYING_TRICK) {
                        Button(
                            onClick = {
                                if (state.profile.hapticsEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                onPlaySelectedCard()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = tableTheme.accentBright,
                                contentColor = tableTheme.deepColor
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("confirm_play_card_button")
                        ) {
                            Text(
                                text = "پخش کارت",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Bottom Player Hand
                PlayerHandFan(
                    hand = snap.myHand,
                    trumpSuit = snap.trumpSuit,
                    legalCards = legalCards,
                    selectedCardId = state.selectedCardId,
                    isMyTurn = isMyTurn && snap.phase == GamePhase.PLAYING_TRICK,
                    accentColor = tableTheme.accentColor,
                    onCardClick = { card ->
                        if (state.profile.hapticsEnabled) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onCardTapped(card)
                    }
                )
            }
        }
    }

    if (showThemePickerDialog) {
        Dialog(onDismissRequest = { showThemePickerDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = tableTheme.deepColor,
                border = BorderStroke(1.5.dp, tableTheme.accentColor)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Palette,
                            contentDescription = null,
                            tint = tableTheme.accentBright
                        )
                        Text(
                            text = "تغییر پس‌زمینه و تم میز",
                            style = MaterialTheme.typography.titleMedium,
                            color = IvoryText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TableThemePickerRow(
                        selectedTheme = tableTheme,
                        onSelectTheme = { chosen ->
                            onSelectTableTheme(chosen)
                            showThemePickerDialog = false
                        }
                    )
                    TextButton(
                        onClick = { showThemePickerDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("بستن")
                    }
                }
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("خروج از میز بازی؟") },
            text = {
                Text(
                    if (state.role == SessionRole.HOST) {
                        "با خروج شما به عنوان میزبان، اتاق برای سایر بازیکنان بسته می‌شود."
                    } else {
                        "آیا مطمئن هستید که می‌خواهید از بازی خارج شوید؟"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        onExitMatch()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RubyDanger),
                    modifier = Modifier.testTag("confirm_exit_game_button")
                ) {
                    Text("خروج", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("ادامه بازی")
                }
            }
        )
    }
}

@Composable
private fun LuxuryScoreboardHud(
    snap: ClientGameSnapshot,
    role: SessionRole,
    tableTheme: TableTheme,
    myMatchScore: Int,
    oppMatchScore: Int,
    myTricks: Int,
    oppTricks: Int,
    onOpenThemePicker: () -> Unit,
    onToggleChat: () -> Unit,
    onRequestExit: () -> Unit
) {
    val kotAlert = when {
        myTricks >= 5 && oppTricks == 0 -> "🔥 شانس کُت برای تیم ما!"
        oppTricks >= 5 && myTricks == 0 -> "⚠️ مراقب کُت حریف باشید!"
        else -> null
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = tableTheme.deepColor.copy(alpha = 0.86f),
        border = BorderStroke(1.2.dp, tableTheme.accentColor.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Utility Row: Hand #, Kot Warning, BT status, Theme, Chat, Exit
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = tableTheme.midColor,
                        border = BorderStroke(1.dp, tableTheme.accentColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "دست ${snap.handNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = tableTheme.accentBright,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }
                    if (role == SessionRole.HOST || role == SessionRole.CLIENT) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = BluetoothBlue.copy(alpha = 0.16f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.BluetoothConnected,
                                    contentDescription = "متصل با بلوتوث",
                                    tint = BluetoothBlue,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "بلوتوث",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BluetoothBlue
                                )
                            }
                        }
                    }
                    if (kotAlert != null) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = AmberWarning.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.7f))
                        ) {
                            Text(
                                text = kotAlert,
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberWarning,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onOpenThemePicker,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("game_theme_picker_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Palette,
                            contentDescription = "تغییر تم میز",
                            tint = tableTheme.accentBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onToggleChat,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("quick_chat_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChatBubbleOutline,
                            contentDescription = "پیام کوتاه",
                            tint = tableTheme.accentBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onRequestExit,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("exit_game_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                            contentDescription = "خروج از بازی",
                            tint = WarmMutedText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Dual Team Scoreboard + Center Trump Shield
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TeamScorePanel(
                    teamName = "تیم ما",
                    accentColor = JadeSuccess,
                    matchScore = myMatchScore,
                    targetHands = snap.targetHands,
                    tricksWon = myTricks,
                    tableTheme = tableTheme,
                    modifier = Modifier.weight(1f)
                )

                // Center Trump Medallion
                TrumpCenterMedallion(
                    trumpSuit = snap.trumpSuit,
                    tableTheme = tableTheme
                )

                TeamScorePanel(
                    teamName = "تیم حریف",
                    accentColor = RubyDanger,
                    matchScore = oppMatchScore,
                    targetHands = snap.targetHands,
                    tricksWon = oppTricks,
                    tableTheme = tableTheme,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TeamScorePanel(
    teamName: String,
    accentColor: Color,
    matchScore: Int,
    targetHands: Int,
    tricksWon: Int,
    tableTheme: TableTheme,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = tableTheme.midColor.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.42f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Match Wins Row ("دست برد")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = teamName,
                    style = MaterialTheme.typography.labelMedium,
                    color = IvoryText,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = tableTheme.accentColor.copy(alpha = 0.18f),
                    border = BorderStroke(0.8.dp, tableTheme.accentColor.copy(alpha = 0.65f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EmojiEvents,
                            contentDescription = "دست‌های برد",
                            tint = tableTheme.accentBright,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "برد $matchScore/$targetHands",
                            style = MaterialTheme.typography.labelSmall,
                            color = tableTheme.accentBright,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Match Hands Won Dot Strip (for targetHands <= 7)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(targetHands.coerceAtMost(7)) { idx ->
                    val won = idx < matchScore
                    Box(
                        modifier = Modifier
                            .size(width = 8.dp, height = 4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (won) tableTheme.accentBright else Color.White.copy(alpha = 0.14f)
                            )
                    )
                }
            }

            // Current Hand Tricks Won ("دست گرفته شده: X از ۷")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "گرفته‌شده",
                    style = MaterialTheme.typography.labelSmall,
                    color = WarmMutedText
                )
                Text(
                    text = "$tricksWon از ۷",
                    style = MaterialTheme.typography.labelMedium,
                    color = accentColor,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // 7-slot visual trick bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                repeat(7) { idx ->
                    val filled = idx < tricksWon
                    val segColor by animateColorAsState(
                        targetValue = if (filled) accentColor else Color.White.copy(alpha = 0.12f),
                        label = "trickSeg"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(segColor)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrumpCenterMedallion(
    trumpSuit: Suit?,
    tableTheme: TableTheme
) {
    val suitColor = when {
        trumpSuit == null -> tableTheme.accentBright
        trumpSuit.isRed -> CrimsonSuit
        else -> ObsidianSuit
    }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (trumpSuit != null) IvoryCard else tableTheme.midColor,
        border = BorderStroke(1.8.dp, tableTheme.accentBright),
        shadowElevation = 6.dp,
        modifier = Modifier.size(width = 64.dp, height = 74.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (trumpSuit != null) {
                Text(
                    text = trumpSuit.symbol,
                    color = suitColor,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 26.sp
                )
                Text(
                    text = trumpSuit.persianName,
                    color = ObsidianSuit,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            } else {
                Text(
                    text = "👑",
                    fontSize = 20.sp
                )
                Text(
                    text = "تعیین حکم",
                    color = tableTheme.accentBright,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun TableSeatBadge(
    player: SeatPlayer,
    roleLabel: String,
    teamColor: Color,
    cardCount: Int,
    isHakem: Boolean,
    isTurn: Boolean,
    tableTheme: TableTheme,
    chatBanner: QuickChatBanner?,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val borderColor by animateColorAsState(
        targetValue = if (isTurn) tableTheme.accentBright else tableTheme.accentColor.copy(alpha = 0.38f),
        label = "seatTurnBorder"
    )
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isTurn) tableTheme.accentColor.copy(alpha = 0.25f) else tableTheme.deepColor.copy(alpha = 0.82f),
            border = BorderStroke(
                width = if (isTurn) 2.2.dp else 1.dp,
                color = borderColor
            ),
            shadowElevation = if (isTurn) 8.dp else 2.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(
                    horizontal = if (compact) 8.dp else 11.dp,
                    vertical = if (compact) 6.dp else 7.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .size(if (compact) 26.dp else 30.dp)
                        .clip(CircleShape)
                        .background(teamColor.copy(alpha = 0.22f))
                        .border(1.2.dp, teamColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (player.isBot) {
                        Icon(
                            imageVector = Icons.Rounded.SmartToy,
                            contentDescription = null,
                            tint = teamColor,
                            modifier = Modifier.size(15.dp)
                        )
                    } else {
                        Text(
                            text = player.name.take(1),
                            color = IvoryText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column {
                    Text(
                        text = buildString {
                            if (isHakem) append("👑 ")
                            append(player.name)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = IvoryText,
                        fontWeight = if (isTurn || isHakem) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = if (compact) 78.dp else 130.dp)
                    )
                    Text(
                        text = "$roleLabel • $cardCount کارت",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isTurn) tableTheme.accentBright else WarmMutedText
                    )
                }

                if (!compact && cardCount > 0) {
                    PlayingCardBack(
                        width = 20.dp,
                        height = 28.dp,
                        cardBackColor = tableTheme.cardBackColor,
                        accentColor = tableTheme.accentColor
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = chatBanner != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut()
        ) {
            chatBanner?.let { banner ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = tableTheme.accentBright
                ) {
                    Text(
                        text = banner.message,
                        color = tableTheme.deepColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChoosingTrumpOverlay(
    isMyHakem: Boolean,
    hakemName: String,
    tableTheme: TableTheme,
    onChooseTrump: (Suit) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .widthIn(max = 360.dp),
        shape = RoundedCornerShape(24.dp),
        color = tableTheme.deepColor.copy(alpha = 0.95f),
        border = BorderStroke(1.8.dp, tableTheme.accentBright),
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isMyHakem) {
                Text(
                    text = "👑 شما حاکم هستید!",
                    style = MaterialTheme.typography.titleMedium,
                    color = tableTheme.accentBright,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "با توجه به ۵ کارت اول خود، خال حکم را انتخاب کنید:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmMutedText,
                    textAlign = TextAlign.Center
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Suit.entries.forEach { suit ->
                        val suitColor = if (suit.isRed) CrimsonSuit else ObsidianSuit
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(70.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onChooseTrump(suit) }
                                .testTag("choose_trump_${suit.name}"),
                            shape = RoundedCornerShape(14.dp),
                            color = IvoryCard,
                            border = BorderStroke(1.5.dp, tableTheme.accentColor)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = suit.symbol,
                                    color = suitColor,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = suit.persianName,
                                    color = ObsidianSuit,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "👑 $hakemName حاکم است",
                    style = MaterialTheme.typography.titleMedium,
                    color = tableTheme.accentBright,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "در حال انتظار برای انتخاب خال حکم...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmMutedText
                )
            }
        }
    }
}

@Composable
private fun PlayerHandFan(
    hand: List<Card>,
    trumpSuit: Suit?,
    legalCards: Set<Card>,
    selectedCardId: String?,
    isMyTurn: Boolean,
    accentColor: Color,
    onCardClick: (Card) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
            .padding(top = 18.dp, bottom = 4.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        if (hand.isEmpty()) return@BoxWithConstraints

        val cardWidth = if (maxWidth < 380.dp) 58.dp else 66.dp
        val cardHeight = if (maxWidth < 380.dp) 88.dp else 98.dp
        val availableWidth = (maxWidth - 8.dp).coerceAtLeast(cardWidth)
        val step = if (hand.size <= 1) {
            0.dp
        } else {
            ((availableWidth - cardWidth) / (hand.size - 1)).coerceIn(22.dp, cardWidth + 6.dp)
        }
        val totalSpan = cardWidth + step * (hand.size - 1)

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(
                modifier = Modifier
                    .width(totalSpan)
                    .height(cardHeight)
            ) {
                hand.forEachIndexed { index, card ->
                    val isLegal = !isMyTurn || card in legalCards
                    PlayingCardFront(
                        card = card,
                        width = cardWidth,
                        height = cardHeight,
                        isTrump = card.suit == trumpSuit,
                        isLegal = isLegal,
                        isSelected = selectedCardId == card.id,
                        accentColor = accentColor,
                        onClick = { onCardClick(card) },
                        modifier = Modifier.offset(x = step * index)
                    )
                }
            }
        }
    }
}

@Composable
private fun HandOverOverlay(
    snap: ClientGameSnapshot,
    myTeam: Int,
    tableTheme: TableTheme,
    canControlNext: Boolean,
    onNextHand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary = snap.lastHandSummary ?: return
    val didWeWin = summary.winningTeam == myTeam
    val title = when {
        summary.isHakemKot -> if (didWeWin) "حاکم‌کُت! (+۳ امتیاز)" else "حاکم‌کُت شدید! (+۳ برای حریف)"
        summary.isKot -> if (didWeWin) "کُت! (+۲ امتیاز)" else "کُت شدید! (+۲ برای حریف)"
        else -> if (didWeWin) "این دست را بردید! (+۱)" else "حریف این دست را برد (+۱)"
    }
    val nextHakemName = snap.players.getOrNull(summary.nextHakemSeat)?.name ?: "حاکم"

    Surface(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .widthIn(max = 350.dp),
        shape = RoundedCornerShape(24.dp),
        color = tableTheme.deepColor.copy(alpha = 0.96f),
        border = BorderStroke(1.8.dp, if (didWeWin) JadeSuccess else tableTheme.accentBright),
        shadowElevation = 14.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.EmojiEvents,
                contentDescription = null,
                tint = if (didWeWin) JadeSuccess else tableTheme.accentBright,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = IvoryText,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "دورهای گرفته‌شده: تیم ۱ (${summary.team1Tricks}) — تیم ۲ (${summary.team2Tricks})",
                style = MaterialTheme.typography.bodyMedium,
                color = WarmMutedText
            )
            Text(
                text = "دست‌های برد کل: ${snap.team1MatchScore} به ${snap.team2MatchScore} (هدف: ${snap.targetHands})",
                style = MaterialTheme.typography.bodyMedium,
                color = tableTheme.accentBright,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "حاکم دست بعد: 👑 $nextHakemName",
                style = MaterialTheme.typography.labelLarge,
                color = IvoryText
            )

            if (canControlNext) {
                Button(
                    onClick = onNextHand,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("next_hand_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tableTheme.accentColor,
                        contentColor = tableTheme.deepColor
                    )
                ) {
                    Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("شروع دست بعدی", fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    text = "منتظر شروع دست بعدی توسط میزبان...",
                    style = MaterialTheme.typography.labelMedium,
                    color = WarmMutedText
                )
            }
        }
    }
}

@Composable
private fun MatchOverOverlay(
    snap: ClientGameSnapshot,
    myTeam: Int,
    tableTheme: TableTheme,
    canRematch: Boolean,
    onRematch: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val myScore = if (myTeam == 1) snap.team1MatchScore else snap.team2MatchScore
    val oppScore = if (myTeam == 1) snap.team2MatchScore else snap.team1MatchScore
    val didWeWin = myScore > oppScore

    Surface(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .widthIn(max = 360.dp),
        shape = RoundedCornerShape(26.dp),
        color = tableTheme.deepColor.copy(alpha = 0.97f),
        border = BorderStroke(2.dp, tableTheme.accentBright),
        shadowElevation = 16.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_victory_crown),
                contentDescription = "تاج قهرمانی حکم",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(116.dp)
                    .clip(CircleShape)
            )
            Text(
                text = if (didWeWin) "قهرمان مسابقه شدید!" else "حریف برنده مسابقه شد",
                style = MaterialTheme.typography.headlineSmall,
                color = if (didWeWin) tableTheme.accentBright else IvoryText,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "نتیجه نهایی دست‌های برد: تیم ما $myScore — حریف $oppScore",
                style = MaterialTheme.typography.titleSmall,
                color = if (didWeWin) JadeSuccess else WarmMutedText
            )

            if (canRematch) {
                Button(
                    onClick = onRematch,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("rematch_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tableTheme.accentColor,
                        contentColor = tableTheme.deepColor
                    )
                ) {
                    Icon(imageVector = Icons.Rounded.Replay, contentDescription = null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("بازی مجدد", fontWeight = FontWeight.Bold)
                }
            }

            OutlinedButton(
                onClick = onExit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("match_over_exit_button"),
                border = BorderStroke(1.dp, tableTheme.accentColor.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = IvoryText)
            ) {
                Text("بازگشت به صفحه اصلی")
            }
        }
    }
}
