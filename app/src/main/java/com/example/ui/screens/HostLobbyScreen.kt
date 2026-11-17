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
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BluetoothConnected
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.game.SeatPlayer
import com.example.ui.components.FeltTableBackground
import com.example.ui.theme.AmberWarning
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
fun HostLobbyScreen(
    state: HokmUiState,
    requiredPermissions: Array<String>,
    onRefreshBluetooth: () -> Unit,
    onRestartServer: () -> Unit,
    onSeatClicked: (Int) -> Unit,
    onSelectTargetHands: (Int) -> Unit,
    onStartMatch: () -> Unit,
    onPlaySoloFallback: () -> Unit,
    onDismissError: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        onRefreshBluetooth()
        onRestartServer()
    }

    val enableBtLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        onRefreshBluetooth()
        onRestartServer()
    }

    val discoverableLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        onRefreshBluetooth()
    }

    val players = state.snapshot?.players ?: emptyList()
    val connectedHumans = players.count { !it.isBot }
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
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("host_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = "بازگشت",
                                tint = BrassGoldBright
                            )
                        }
                        Column {
                            Text(
                                text = "میز گروهی بلوتوث (میزبان)",
                                style = MaterialTheme.typography.titleLarge,
                                color = IvoryText,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$connectedHumans از ۴ بازیکن واقعی متصل",
                                style = MaterialTheme.typography.labelMedium,
                                color = JadeSuccess
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (state.isHosting) BluetoothBlue.copy(alpha = 0.2f) else AmberWarning.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, if (state.isHosting) BluetoothBlue else AmberWarning)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.BluetoothConnected,
                                contentDescription = null,
                                tint = if (state.isHosting) BluetoothBlue else AmberWarning,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (state.isHosting) "اتاق باز است" else "غیرفعال",
                                style = MaterialTheme.typography.labelSmall,
                                color = IvoryText
                            )
                        }
                    }
                }

                // Error notice
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

                // Bluetooth Status / Permissions Card
                BluetoothSetupCard(
                    isSupported = state.isBluetoothSupported,
                    hasPermissions = state.hasBluetoothPermissions,
                    isEnabled = state.isBluetoothEnabled,
                    isHostMode = true,
                    onRequestPermissions = { permissionLauncher.launch(requiredPermissions) },
                    onEnableBluetooth = {
                        enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    },
                    onMakeDiscoverable = {
                        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                        }
                        discoverableLauncher.launch(intent)
                    },
                    onPlaySoloFallback = onPlaySoloFallback
                )

                // 4-Seat Table Arrangement (Tap two seats to swap!)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = EmeraldFeltMid.copy(alpha = 0.85f),
                    border = BorderStroke(1.2.dp, BrassGold.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "چیدمان صندلی‌ها و تیم‌ها",
                                style = MaterialTheme.typography.titleMedium,
                                color = BrassGoldBright,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SwapHoriz,
                                    contentDescription = null,
                                    tint = WarmMutedText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (state.selectedSwapSeat != null) "مقصد جابه‌جایی را بزنید"
                                    else "برای جابه‌جایی صندلی‌ها لمس کنید",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (state.selectedSwapSeat != null) BrassGoldBright else WarmMutedText
                                )
                            }
                        }

                        // Seat 2 (Top - Partner of Seat 0)
                        val seat2 = players.getOrNull(2)
                        val seat1 = players.getOrNull(1)
                        val seat3 = players.getOrNull(3)
                        val seat0 = players.getOrNull(0)

                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            seat2?.let {
                                LobbySeatPill(
                                    player = it,
                                    teamLabel = "تیم ۱ (یار شما)",
                                    isSelected = state.selectedSwapSeat == 2,
                                    onClick = { onSeatClicked(2) },
                                    modifier = Modifier.fillMaxWidth(0.66f)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            seat1?.let {
                                LobbySeatPill(
                                    player = it,
                                    teamLabel = "تیم ۲ (حریف)",
                                    isSelected = state.selectedSwapSeat == 1,
                                    onClick = { onSeatClicked(1) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            seat3?.let {
                                LobbySeatPill(
                                    player = it,
                                    teamLabel = "تیم ۲ (حریف)",
                                    isSelected = state.selectedSwapSeat == 3,
                                    onClick = { onSeatClicked(3) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            seat0?.let {
                                LobbySeatPill(
                                    player = it,
                                    teamLabel = "تیم ۱ (شما - میزبان)",
                                    isSelected = state.selectedSwapSeat == 0,
                                    onClick = { onSeatClicked(0) },
                                    modifier = Modifier.fillMaxWidth(0.66f)
                                )
                            }
                        }

                        Text(
                            text = "صندلی‌های خالی به‌صورت خودکار با ربات‌های هوشمند پر می‌شوند تا با ۲، ۳ یا ۴ نفر بازی کنید.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WarmMutedText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Target Hands Selection
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = EmeraldFeltMid.copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, BrassGold.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "تعداد دست برای برد مسابقه",
                            style = MaterialTheme.typography.titleSmall,
                            color = IvoryText
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(3 to "۳ دست (سریع)", 5 to "۵ دست", 7 to "۷ دست (استاندارد)").forEach { (value, label) ->
                                val selected = state.profile.preferredTargetHands == value
                                FilterChip(
                                    selected = selected,
                                    onClick = { onSelectTargetHands(value) },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrassGold,
                                        selectedLabelColor = EmeraldFeltDeep
                                    ),
                                    modifier = Modifier.testTag("target_hands_$value")
                                )
                            }
                        }
                    }
                }

                // Start Game Button
                Button(
                    onClick = onStartMatch,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("start_hosted_match_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrassGold,
                        contentColor = EmeraldFeltDeep
                    )
                ) {
                    Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = if (connectedHumans > 1) "شروع بازی گروهی ($connectedHumans نفر)" else "شروع بازی (همراه با ربات‌ها)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun LobbySeatPill(
    player: SeatPlayer,
    teamLabel: String,
    isSelected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val accent = when {
        isSelected -> BrassGoldBright
        !player.isBot -> JadeSuccess
        else -> WarmMutedText
    }
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .testTag("lobby_seat_${player.seatIndex}"),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) BrassGold.copy(alpha = 0.24f) else EmeraldFeltDeep.copy(alpha = 0.68f),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = accent.copy(alpha = if (isSelected) 1f else 0.45f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = accent.copy(alpha = 0.2f),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (player.isBot) Icons.Rounded.SmartToy else Icons.Rounded.Person,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = IvoryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = teamLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = accent
                )
            }
        }
    }
}

@Composable
fun BluetoothSetupCard(
    isSupported: Boolean,
    hasPermissions: Boolean,
    isEnabled: Boolean,
    isHostMode: Boolean,
    onRequestPermissions: () -> Unit,
    onEnableBluetooth: () -> Unit,
    onMakeDiscoverable: () -> Unit,
    onPlaySoloFallback: () -> Unit
) {
    if (!isSupported) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = AmberWarning.copy(alpha = 0.16f),
            border = BorderStroke(1.dp, AmberWarning)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "سخت‌افزار بلوتوث در این دستگاه یافت نشد",
                    style = MaterialTheme.typography.titleSmall,
                    color = IvoryText,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "برای اتصال گروهی روی گوشی واقعی اجرا کنید، یا همین حالا بازی تمرینی با ربات‌ها را امتحان کنید.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmMutedText
                )
                OutlinedButton(onClick = onPlaySoloFallback) {
                    Text("بازی تمرینی با ربات‌ها")
                }
            }
        }
        return
    }

    if (!hasPermissions) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = BluetoothBlue.copy(alpha = 0.16f),
            border = BorderStroke(1.dp, BluetoothBlue)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "نیاز به دسترسی بلوتوث",
                    style = MaterialTheme.typography.titleSmall,
                    color = IvoryText,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "برای ساخت میز و اتصال به گوشی‌های دوستان، دسترسی دستگاه‌های نزدیک (بلوتوث) را تأیید کنید.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmMutedText
                )
                Button(
                    onClick = onRequestPermissions,
                    modifier = Modifier.testTag("grant_bt_permission_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BluetoothBlue,
                        contentColor = EmeraldFeltDeep
                    )
                ) {
                    Icon(imageVector = Icons.Rounded.Bluetooth, contentDescription = null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("اعطای دسترسی بلوتوث", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    if (!isEnabled) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = AmberWarning.copy(alpha = 0.16f),
            border = BorderStroke(1.dp, AmberWarning)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "بلوتوث گوشی خاموش است",
                    style = MaterialTheme.typography.titleSmall,
                    color = IvoryText
                )
                Button(
                    onClick = onEnableBluetooth,
                    modifier = Modifier.testTag("enable_bt_button")
                ) {
                    Text("روشن کردن")
                }
            }
        }
        return
    }

    if (isHostMode) {
        OutlinedButton(
            onClick = onMakeDiscoverable,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("make_discoverable_button"),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, BluetoothBlue.copy(alpha = 0.7f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = IvoryText)
        ) {
            Icon(
                imageVector = Icons.Rounded.Visibility,
                contentDescription = null,
                tint = BluetoothBlue
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text("قابل مشاهده شدن برای دوستان (۵ دقیقه)")
        }
    }
}
