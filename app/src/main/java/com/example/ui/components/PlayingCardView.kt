package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.Card
import com.example.game.Rank
import com.example.game.Suit
import com.example.ui.theme.BrassGold
import com.example.ui.theme.BrassGoldBright
import com.example.ui.theme.CrimsonSuit
import com.example.ui.theme.IvoryCard
import com.example.ui.theme.IvoryCardShade
import com.example.ui.theme.IvoryText
import com.example.ui.theme.JadeSuccess
import com.example.ui.theme.ObsidianSuit
import com.example.ui.theme.TableTheme
import com.example.ui.theme.WarmMutedText

@Composable
fun FeltTableBackground(
    tableTheme: TableTheme = TableTheme.ROYAL_EMERALD,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(tableTheme.deepColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.44f)
            val radius = maxOf(size.width, size.height) * 0.78f

            // Multi-stop luxury felt spotlight gradient
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        tableTheme.centerColor,
                        tableTheme.midColor,
                        tableTheme.deepColor
                    ),
                    center = center,
                    radius = radius
                )
            )

            // Subtle diagonal velvet weave lines
            val step = 18.dp.toPx()
            val weaveColor = Color.White.copy(alpha = 0.022f)
            var x = -size.height
            while (x < size.width + size.height) {
                drawLine(
                    color = weaveColor,
                    start = Offset(x, 0f),
                    end = Offset(x + size.height, size.height),
                    strokeWidth = 1.dp.toPx()
                )
                x += step
            }

            // Outer walnut/mahogany beveled rail border
            val railStroke = 10.dp.toPx()
            drawRoundRect(
                color = tableTheme.railColor.copy(alpha = 0.85f),
                topLeft = Offset(0f, 0f),
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                style = Stroke(width = railStroke)
            )

            // Double brass filigree inlay border
            val outerInset = 11.dp.toPx()
            drawRoundRect(
                color = tableTheme.accentColor.copy(alpha = 0.45f),
                topLeft = Offset(outerInset, outerInset),
                size = Size(size.width - outerInset * 2, size.height - outerInset * 2),
                cornerRadius = CornerRadius(26.dp.toPx(), 26.dp.toPx()),
                style = Stroke(width = 1.6.dp.toPx())
            )
            val innerInset = 17.dp.toPx()
            drawRoundRect(
                color = tableTheme.accentColor.copy(alpha = 0.18f),
                topLeft = Offset(innerInset, innerInset),
                size = Size(size.width - innerInset * 2, size.height - innerInset * 2),
                cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx()),
                style = Stroke(width = 0.9.dp.toPx())
            )

            // Center Persian 8-pointed Shamseh medallion
            val cx = size.width / 2f
            val cy = size.height * 0.44f
            val medRadius = minOf(size.width, size.height) * 0.21f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        tableTheme.accentColor.copy(alpha = 0.09f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = medRadius * 1.35f
                ),
                radius = medRadius * 1.35f,
                center = Offset(cx, cy)
            )

            drawCircle(
                color = tableTheme.accentColor.copy(alpha = 0.14f),
                radius = medRadius,
                center = Offset(cx, cy),
                style = Stroke(width = 1.2.dp.toPx())
            )
            drawCircle(
                color = tableTheme.accentColor.copy(alpha = 0.09f),
                radius = medRadius * 0.72f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.dp.toPx())
            )

            // Two rotated squares forming an 8-point Shamseh star
            val sqSize = medRadius * 1.15f
            listOf(0f, 45f).forEach { angle ->
                rotate(degrees = angle, pivot = Offset(cx, cy)) {
                    drawRect(
                        color = tableTheme.accentColor.copy(alpha = 0.12f),
                        topLeft = Offset(cx - sqSize / 2f, cy - sqSize / 2f),
                        size = Size(sqSize, sqSize),
                        style = Stroke(width = 1.1.dp.toPx())
                    )
                }
            }
        }
        content()
    }
}

@Composable
fun PlayingCardFront(
    card: Card,
    modifier: Modifier = Modifier,
    width: Dp = 66.dp,
    height: Dp = 98.dp,
    isTrump: Boolean = false,
    isLegal: Boolean = true,
    isSelected: Boolean = false,
    isWinningCard: Boolean = false,
    accentColor: Color = BrassGold,
    onClick: (() -> Unit)? = null
) {
    val liftOffset by animateDpAsState(
        targetValue = if (isSelected) (-20).dp else 0.dp,
        animationSpec = spring(stiffness = 500f),
        label = "cardLift"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            isWinningCard -> JadeSuccess
            isSelected -> BrassGoldBright
            isTrump -> accentColor
            else -> Color(0xFFD6CEC0)
        },
        label = "cardBorder"
    )
    val suitColor = if (card.suit.isRed) CrimsonSuit else ObsidianSuit
    val contentAlpha = if (isLegal) 1f else 0.54f
    val cardDescription = "${card.rank.persianName} ${card.suit.persianName}"

    Surface(
        modifier = modifier
            .offset(y = liftOffset)
            .size(width = width, height = height)
            .shadow(
                elevation = when {
                    isWinningCard -> 16.dp
                    isSelected -> 14.dp
                    isTrump -> 7.dp
                    else -> 5.dp
                },
                shape = RoundedCornerShape(11.dp),
                ambientColor = if (isWinningCard) JadeSuccess else Color.Black,
                spotColor = if (isSelected || isWinningCard) borderColor else Color.Black
            )
            .clip(RoundedCornerShape(11.dp))
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .semantics { contentDescription = cardDescription }
            .testTag("card_${card.id}"),
        shape = RoundedCornerShape(11.dp),
        color = IvoryCard,
        border = BorderStroke(
            width = when {
                isSelected || isWinningCard -> 2.4.dp
                isTrump -> 1.6.dp
                else -> 1.dp
            },
            color = borderColor
        )
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(IvoryCard, IvoryCardShade.copy(alpha = 0.55f))
                        )
                    )
                    .alpha(contentAlpha)
                    .padding(horizontal = 5.dp, vertical = 4.dp)
            ) {
                // Subtle inner hairline frame on court cards & Aces
                if (card.rank.power >= Rank.JACK.power || isTrump) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                            .border(
                                width = 0.6.dp,
                                color = if (isTrump) accentColor.copy(alpha = 0.4f)
                                else suitColor.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(8.dp)
                            )
                    )
                }

                // Top-left corner index
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = card.rank.symbol,
                        color = suitColor,
                        fontSize = if (width < 60.dp) 12.sp else 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 14.sp
                    )
                    Text(
                        text = card.suit.symbol,
                        color = suitColor,
                        fontSize = if (width < 60.dp) 11.sp else 13.sp,
                        lineHeight = 12.sp
                    )
                }

                // Trump star badge
                if (isTrump) {
                    Text(
                        text = "★",
                        color = accentColor,
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }

                // Center pip / court emblem
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    if (card.rank.power >= Rank.JACK.power && card.rank != Rank.ACE) {
                        val badge = when (card.rank) {
                            Rank.KING -> "♔"
                            Rank.QUEEN -> "♕"
                            Rank.JACK -> "♖"
                            else -> ""
                        }
                        Text(
                            text = badge,
                            color = accentColor.copy(alpha = 0.9f),
                            fontSize = if (width < 60.dp) 13.sp else 16.sp,
                            lineHeight = 16.sp
                        )
                    }
                    Text(
                        text = card.suit.symbol,
                        color = suitColor,
                        fontSize = when {
                            card.rank == Rank.ACE && width >= 60.dp -> 32.sp
                            width < 60.dp -> 20.sp
                            else -> 24.sp
                        }
                    )
                }

                // Bottom-right corner index
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Text(
                        text = card.rank.symbol,
                        color = suitColor.copy(alpha = 0.78f),
                        fontSize = if (width < 60.dp) 10.sp else 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PlayingCardBack(
    modifier: Modifier = Modifier,
    width: Dp = 36.dp,
    height: Dp = 52.dp,
    cardBackColor: Color = Color(0xFF0A2B22),
    accentColor: Color = BrassGold
) {
    Box(
        modifier = modifier
            .size(width = width, height = height)
            .shadow(3.dp, RoundedCornerShape(7.dp))
            .clip(RoundedCornerShape(7.dp))
            .background(cardBackColor)
            .border(1.2.dp, accentColor.copy(alpha = 0.85f), RoundedCornerShape(7.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(3.dp)) {
            drawRect(
                color = accentColor.copy(alpha = 0.35f),
                style = Stroke(width = 0.9.dp.toPx())
            )
            val cx = size.width / 2f
            val cy = size.height / 2f
            val rx = size.width * 0.3f
            val ry = size.height * 0.3f
            val diamond = Path().apply {
                moveTo(cx, cy - ry)
                lineTo(cx + rx, cy)
                lineTo(cx, cy + ry)
                lineTo(cx - rx, cy)
                close()
            }
            drawPath(diamond, color = accentColor.copy(alpha = 0.65f), style = Stroke(width = 1.1.dp.toPx()))
            drawCircle(
                color = accentColor.copy(alpha = 0.5f),
                radius = minOf(size.width, size.height) * 0.11f,
                center = Offset(cx, cy)
            )
        }
    }
}

@Composable
fun SuitChipBadge(
    suit: Suit,
    modifier: Modifier = Modifier,
    accentColor: Color = BrassGold
) {
    val suitColor = if (suit.isRed) CrimsonSuit else ObsidianSuit
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = IvoryCard,
        border = BorderStroke(1.8.dp, accentColor),
        shadowElevation = 4.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text(
                text = suit.symbol,
                color = suitColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "حکم: ${suit.persianName}",
                color = ObsidianSuit,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TableThemePickerRow(
    selectedTheme: TableTheme,
    onSelectTheme: (TableTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(TableTheme.entries) { theme ->
            val isSelected = theme == selectedTheme
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelectTheme(theme) }
                    .testTag("table_theme_${theme.id}"),
                shape = RoundedCornerShape(16.dp),
                color = theme.midColor.copy(alpha = 0.92f),
                border = BorderStroke(
                    width = if (isSelected) 2.2.dp else 1.dp,
                    color = if (isSelected) theme.accentBright else theme.accentColor.copy(alpha = 0.35f)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(theme.centerColor, theme.deepColor)
                                )
                            )
                            .border(1.5.dp, theme.accentBright, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "انتخاب‌شده",
                                tint = theme.accentBright,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = theme.persianTitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = IvoryText,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = theme.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = WarmMutedText
                        )
                    }
                }
            }
        }
    }
}
