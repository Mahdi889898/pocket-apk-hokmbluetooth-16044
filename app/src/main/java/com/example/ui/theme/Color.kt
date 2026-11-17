package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Persian Velvet & Brass Card Lounge Palette
val EmeraldFeltDeep = Color(0xFF061E16)
val EmeraldFelt = Color(0xFF0D3B2E)
val EmeraldFeltMid = Color(0xFF134D3C)
val EmeraldFeltLight = Color(0xFF1A614C)

val WalnutDark = Color(0xFF1E110B)
val WalnutSurface = Color(0xFF2B1911)

val BrassGold = Color(0xFFD4AF37)
val BrassGoldBright = Color(0xFFF4D57A)
val BrassGoldSoft = Color(0xFF9E8129)

val IvoryCard = Color(0xFFFFFCF4)
val IvoryCardShade = Color(0xFFF2E8D5)
val IvoryText = Color(0xFFF6F1E4)
val WarmMutedText = Color(0xFFB7C9C1)

val CrimsonSuit = Color(0xFFC62828)
val ObsidianSuit = Color(0xFF181B20)

val JadeSuccess = Color(0xFF34D399)
val AmberWarning = Color(0xFFFBBF24)
val RubyDanger = Color(0xFFEF5350)
val BluetoothBlue = Color(0xFF60A5FA)
val RoseMahlin = Color(0xFFF9A8D4)

enum class TableTheme(
    val id: String,
    val persianTitle: String,
    val subtitle: String,
    val centerColor: Color,
    val midColor: Color,
    val deepColor: Color,
    val railColor: Color,
    val accentColor: Color,
    val accentBright: Color,
    val cardBackColor: Color
) {
    ROYAL_EMERALD(
        id = "royal_emerald",
        persianTitle = "زمرد سلطنتی",
        subtitle = "مخمل کلاسیک با قاب طلایی",
        centerColor = Color(0xFF1B6952),
        midColor = Color(0xFF0D3B2E),
        deepColor = Color(0xFF051A13),
        railColor = Color(0xFF2A170E),
        accentColor = Color(0xFFD4AF37),
        accentBright = Color(0xFFF4D57A),
        cardBackColor = Color(0xFF0A2B22)
    ),
    SAPPHIRE_LOUNGE(
        id = "sapphire_lounge",
        persianTitle = "یاقوت کبود",
        subtitle = "سرمه‌ای اشرافی و طلای کهربایی",
        centerColor = Color(0xFF1D467E),
        midColor = Color(0xFF0F274B),
        deepColor = Color(0xFF061022),
        railColor = Color(0xFF171A26),
        accentColor = Color(0xFFE0B44A),
        accentBright = Color(0xFFFFE082),
        cardBackColor = Color(0xFF0B1D3A)
    ),
    CRIMSON_VELVET(
        id = "crimson_velvet",
        persianTitle = "زرشکی مخملی",
        subtitle = "قالی ایرانی با حاشیه شامپاینی",
        centerColor = Color(0xFF6D1931),
        midColor = Color(0xFF420D1C),
        deepColor = Color(0xFF1D040B),
        railColor = Color(0xFF26110C),
        accentColor = Color(0xFFE3BA6D),
        accentBright = Color(0xFFFBE2AE),
        cardBackColor = Color(0xFF360916)
    ),
    OBSIDIAN_GOLD(
        id = "obsidian_gold",
        persianTitle = "آبنوس و طلا",
        subtitle = "مشکی لوکس با درخشش طلا",
        centerColor = Color(0xFF2B2F37),
        midColor = Color(0xFF17191E),
        deepColor = Color(0xFF090A0D),
        railColor = Color(0xFF1A140D),
        accentColor = Color(0xFFE8C15A),
        accentBright = Color(0xFFFFE599),
        cardBackColor = Color(0xFF15171C)
    );

    companion object {
        fun fromId(id: String?): TableTheme =
            entries.firstOrNull { it.id == id } ?: ROYAL_EMERALD
    }
}
