package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.FeltTableBackground
import com.example.ui.theme.BluetoothBlue
import com.example.ui.theme.BrassGold
import com.example.ui.theme.BrassGoldBright
import com.example.ui.theme.EmeraldFeltMid
import com.example.ui.theme.IvoryText
import com.example.ui.theme.JadeSuccess
import com.example.ui.theme.TableTheme
import com.example.ui.theme.WarmMutedText

@Composable
fun RulesScreen(
    tableTheme: TableTheme = TableTheme.ROYAL_EMERALD,
    onBack: () -> Unit
) {
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
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("rules_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = "بازگشت",
                            tint = BrassGoldBright
                        )
                    }
                    Text(
                        text = "قوانین حکم و اتصال بلوتوثی",
                        style = MaterialTheme.typography.headlineSmall,
                        color = IvoryText,
                        fontWeight = FontWeight.Bold
                    )
                }

                RuleSectionCard(
                    icon = Icons.Rounded.Bluetooth,
                    accent = BluetoothBlue,
                    title = "نحوه اتصال گروهی با بلوتوث (۲ تا ۴ گوشی)",
                    bullets = listOf(
                        "۱. یک نفر روی «ساخت میز گروهی (بلوتوث)» بزند تا گوشی او میزبان شود و دکمه «قابل مشاهده شدن» را بزند.",
                        "۲. بقیه دوستان (تا ۳ نفر) وارد «اتصال به میزبان» شوند، اسکن را بزنند و گوشی میزبان را انتخاب کنند (در صورت نیاز یک‌بار کد جفت‌سازی بلوتوث را تأیید کنید).",
                        "۳. میزبان می‌تواند روی صندلی‌ها بزند تا جای یار و حریف را جابه‌جا کند. صندلی‌های خالی به‌طور خودکار با ربات هوشمند پر می‌شوند!"
                    )
                )

                RuleSectionCard(
                    icon = Icons.Rounded.Gavel,
                    accent = BrassGold,
                    title = "شروع دست و انتخاب حکم",
                    bullets = listOf(
                        "• در ابتدای بازی به نوبت کارت پخش می‌شود؛ هر کس اولین «تک (آس)» را بگیرد حاکم اول می‌شود.",
                        "• ابتدا ۵ کارت به هر بازیکن داده می‌شود و حاکم از میان ۴ خال (پیک، دل، گشنیز، خشت) خال حکم را انتخاب می‌کند.",
                        "• سپس ۸ کارت باقی‌مانده پخش می‌شود تا هر بازیکن ۱۳ کارت داشته باشد."
                    )
                )

                RuleSectionCard(
                    icon = Icons.Rounded.Groups,
                    accent = JadeSuccess,
                    title = "قانون بازی، کُت و حاکم‌کُت",
                    bullets = listOf(
                        "• بازیکنان روبه‌رو هم‌تیمی هستند. در هر دور باید خال کارت اول (زمینه) را بازی کنید؛ اگر ندارید، می‌توانید با خال حکم «بُرش» بزنید یا کارت دیگری بیندازید.",
                        "• اولین تیمی که ۷ دور را ببرد برنده آن دست است (۱ امتیاز).",
                        "• کُت (+۲ امتیاز): اگر تیم برنده ۷ بر ۰ ببرد.",
                        "• حاکم‌کُت (+۳ امتیاز): اگر تیم غیرحاکم، تیم حاکم را ۷ بر ۰ ببرد!",
                        "• اگر تیم حاکم برنده شود، حاکم در دست بعد ثابت می‌ماند؛ وگرنه حاکم به نفر بعدی (ساعتگرد) می‌رسد."
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun RuleSectionCard(
    icon: ImageVector,
    accent: androidx.compose.ui.graphics.Color,
    title: String,
    bullets: List<String>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = EmeraldFeltMid.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accent)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = IvoryText,
                    fontWeight = FontWeight.Bold
                )
            }
            bullets.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmMutedText
                )
            }
        }
    }
}
