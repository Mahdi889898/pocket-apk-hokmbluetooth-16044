package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.BluetoothSearching
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.components.FeltTableBackground
import com.example.ui.components.TableThemePickerRow
import com.example.ui.theme.BluetoothBlue
import com.example.ui.theme.BrassGold
import com.example.ui.theme.BrassGoldBright
import com.example.ui.theme.EmeraldFeltDeep
import com.example.ui.theme.IvoryText
import com.example.ui.theme.JadeSuccess
import com.example.ui.theme.RoseMahlin
import com.example.ui.theme.TableTheme
import com.example.ui.theme.WarmMutedText
import com.example.viewmodel.HokmUiState

@Composable
fun HomeScreen(
    state: HokmUiState,
    onHostBluetoothClick: () -> Unit,
    onJoinBluetoothClick: () -> Unit,
    onSoloPracticeClick: () -> Unit,
    onRulesClick: () -> Unit,
    onUpdateName: (String) -> Unit,
    onSelectTableTheme: (TableTheme) -> Unit,
    onToggleHaptics: (Boolean) -> Unit
) {
    var showNameDialog by remember { mutableStateOf(false) }
    var showMahlinModal by remember { mutableStateOf(false) }
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
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Hero Banner Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.2.dp, tableTheme.accentColor.copy(alpha = 0.55f)),
                    colors = CardDefaults.cardColors(containerColor = tableTheme.midColor)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(148.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hokm_hero),
                            contentDescription = "میز بازی حکم",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            tableTheme.deepColor.copy(alpha = 0.25f),
                                            tableTheme.deepColor.copy(alpha = 0.94f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = tableTheme.accentColor.copy(alpha = 0.22f),
                                border = BorderStroke(1.dp, tableTheme.accentColor.copy(alpha = 0.65f))
                            ) {
                                Text(
                                    text = "♠ ♥ ♦ ♣  حکم چهارنفره بلوتوثی",
                                    color = tableTheme.accentBright,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "محفل حکم بلوتوثی",
                                style = MaterialTheme.typography.headlineMedium,
                                color = IvoryText,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "گوشی‌ها را با بلوتوث به هم وصل کنید و در کنار هم گروهی بازی کنید.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = WarmMutedText
                            )
                        }
                    }
                }

                // Player Profile & Stats Strip
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = tableTheme.midColor.copy(alpha = 0.88f),
                    border = BorderStroke(1.dp, tableTheme.accentColor.copy(alpha = 0.32f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = tableTheme.accentColor.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, tableTheme.accentColor),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = state.profile.playerName.take(1),
                                        color = tableTheme.accentBright,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = state.profile.playerName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = IvoryText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    IconButton(
                                        onClick = { showNameDialog = true },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .testTag("edit_name_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Edit,
                                            contentDescription = "ویرایش نام بازیکن",
                                            tint = tableTheme.accentColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "بازی: ${state.profile.matchesPlayed}  •  برد: ${state.profile.matchesWon}  •  کُت: ${state.profile.kotsScored}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = WarmMutedText
                                )
                            }
                        }
                    }
                }

                // Primary Mode Cards
                ModeActionCard(
                    title = "ساخت میز گروهی (بلوتوث)",
                    subtitle = "شما میزبان شوید؛ تا ۳ دوست دیگر با بلوتوث به شما وصل شوند.",
                    icon = Icons.Rounded.Groups,
                    accentColor = tableTheme.accentColor,
                    surfaceColor = tableTheme.midColor,
                    badgeText = "میزبان",
                    testTag = "host_room_button",
                    onClick = onHostBluetoothClick
                )

                ModeActionCard(
                    title = "اتصال به میزبان (بلوتوث)",
                    subtitle = "دستگاه میزبان را انتخاب کنید و وارد میز گروهی شوید.",
                    icon = Icons.AutoMirrored.Rounded.BluetoothSearching,
                    accentColor = BluetoothBlue,
                    surfaceColor = tableTheme.midColor,
                    badgeText = "عضو",
                    testTag = "join_room_button",
                    onClick = onJoinBluetoothClick
                )

                ModeActionCard(
                    title = "بازی تمرینی آفلاین (با ربات)",
                    subtitle = "تک‌نفره با ۳ ربات هوشمند بدون نیاز به دستگاه دیگر بازی کنید.",
                    icon = Icons.Rounded.SmartToy,
                    accentColor = JadeSuccess,
                    surfaceColor = tableTheme.midColor,
                    badgeText = "تک‌نفره",
                    testTag = "solo_practice_button",
                    onClick = onSoloPracticeClick
                )

                // Table Background / Theme Selector
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = tableTheme.midColor.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, tableTheme.accentColor.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Palette,
                                contentDescription = null,
                                tint = tableTheme.accentBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "انتخاب پس‌زمینه و تم میز بازی",
                                style = MaterialTheme.typography.titleSmall,
                                color = IvoryText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        TableThemePickerRow(
                            selectedTheme = tableTheme,
                            onSelectTheme = onSelectTableTheme
                        )
                    }
                }

                // Dedication Card: "تقدیم به دختر قشنگم ماهلین"
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { showMahlinModal = true }
                        .testTag("mahlin_dedication_card"),
                    shape = RoundedCornerShape(22.dp),
                    color = tableTheme.deepColor.copy(alpha = 0.78f),
                    border = BorderStroke(1.4.dp, RoseMahlin.copy(alpha = 0.65f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        RoseMahlin.copy(alpha = 0.14f),
                                        tableTheme.accentColor.copy(alpha = 0.10f)
                                    )
                                )
                            )
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .border(2.2.dp, tableTheme.accentBright, CircleShape)
                                .background(tableTheme.midColor)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_mahlin),
                                contentDescription = "عکس ماهلین عزیز",
                                contentScale = ContentScale.Crop,
                                alignment = BiasAlignment(0f, -0.45f),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Favorite,
                                    contentDescription = null,
                                    tint = RoseMahlin,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "تقدیم به دختر قشنگم ماهلین",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = tableTheme.accentBright,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "لبخند تو قشنگ‌ترین هدیه زندگی است. برای دیدن قاب بزرگ‌تر لمس کنید.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = IvoryText.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                // Footer Controls: Rules & Haptics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onRulesClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("rules_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, tableTheme.accentColor.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = IvoryText)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                            contentDescription = null,
                            tint = tableTheme.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("قوانین و راهنمای اتصال")
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = tableTheme.midColor.copy(alpha = 0.75f),
                        border = BorderStroke(1.dp, tableTheme.accentColor.copy(alpha = 0.25f)),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Vibration,
                                contentDescription = "لرزش لمسی",
                                tint = tableTheme.accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Switch(
                                checked = state.profile.hapticsEnabled,
                                onCheckedChange = onToggleHaptics,
                                modifier = Modifier.testTag("haptics_switch")
                            )
                        }
                    }
                }
            }
        }
    }

    if (showMahlinModal) {
        Dialog(onDismissRequest = { showMahlinModal = false }) {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = tableTheme.deepColor,
                border = BorderStroke(2.dp, tableTheme.accentBright)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = { showMahlinModal = false },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "بستن",
                                tint = IvoryText
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Favorite,
                                contentDescription = null,
                                tint = RoseMahlin,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "تقدیم به دختر قشنگم ماهلین",
                                style = MaterialTheme.typography.titleMedium,
                                color = tableTheme.accentBright,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Image(
                        painter = painterResource(id = R.drawable.img_mahlin),
                        contentDescription = "عکس ماهلین",
                        contentScale = ContentScale.Crop,
                        alignment = BiasAlignment(0f, -0.35f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.78f)
                            .clip(RoundedCornerShape(20.dp))
                            .border(2.dp, RoseMahlin.copy(alpha = 0.75f), RoundedCornerShape(20.dp))
                    )

                    Text(
                        text = "با آرزوی بهترین لحظه‌ها و لبخندهای همیشگی برای ماهلین نازنین ❤️",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IvoryText,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (showNameDialog) {
        var nameInput by remember(state.profile.playerName) {
            mutableStateOf(state.profile.playerName)
        }
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("نام نمایشی شما روی میز") },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { if (it.length <= 18) nameInput = it },
                    singleLine = true,
                    label = { Text("نام بازیکن") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("player_name_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateName(nameInput)
                        showNameDialog = false
                    },
                    modifier = Modifier.testTag("save_name_button")
                ) {
                    Text("ذخیره")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
private fun ModeActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    surfaceColor: Color,
    badgeText: String,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        color = surfaceColor.copy(alpha = 0.92f),
        border = BorderStroke(1.2.dp, accentColor.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = accentColor.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = IvoryText,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = accentColor.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmMutedText
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = null,
                tint = accentColor.copy(alpha = 0.8f)
            )
        }
    }
}
