package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val HokmDarkColorScheme = darkColorScheme(
    primary = BrassGold,
    onPrimary = EmeraldFeltDeep,
    primaryContainer = EmeraldFeltLight,
    onPrimaryContainer = BrassGoldBright,
    secondary = JadeSuccess,
    onSecondary = EmeraldFeltDeep,
    secondaryContainer = EmeraldFeltMid,
    onSecondaryContainer = IvoryText,
    tertiary = BluetoothBlue,
    onTertiary = EmeraldFeltDeep,
    background = EmeraldFeltDeep,
    onBackground = IvoryText,
    surface = EmeraldFelt,
    onSurface = IvoryText,
    surfaceVariant = EmeraldFeltMid,
    onSurfaceVariant = WarmMutedText,
    outline = BrassGoldSoft,
    outlineVariant = Color(0xFF235C4B),
    error = RubyDanger,
    onError = Color.White
)

val HokmShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun HokmTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = HokmDarkColorScheme,
            typography = HokmTypography,
            shapes = HokmShapes,
            content = content
        )
    }
}
