package com.example.game

import com.squareup.moshi.JsonClass

enum class Suit(
    val persianName: String,
    val symbol: String,
    val isRed: Boolean
) {
    SPADES("پیک", "♠", false),
    HEARTS("دل", "♥", true),
    CLUBS("گشنیز", "♣", false),
    DIAMONDS("خشت", "♦", true)
}

enum class Rank(
    val power: Int,
    val symbol: String,
    val persianName: String
) {
    TWO(2, "2", "۲"),
    THREE(3, "3", "۳"),
    FOUR(4, "4", "۴"),
    FIVE(5, "5", "۵"),
    SIX(6, "6", "۶"),
    SEVEN(7, "7", "۷"),
    EIGHT(8, "8", "۸"),
    NINE(9, "9", "۹"),
    TEN(10, "10", "۱۰"),
    JACK(11, "J", "سرباز"),
    QUEEN(12, "Q", "بی‌بی"),
    KING(13, "K", "شاه"),
    ACE(14, "A", "تک")
}

@JsonClass(generateAdapter = true)
data class Card(
    val suit: Suit,
    val rank: Rank
) {
    val id: String get() = "${suit.name}_${rank.name}"

    companion object {
        fun fullDeck(): List<Card> = buildList(52) {
            for (suit in Suit.entries) {
                for (rank in Rank.entries) {
                    add(Card(suit, rank))
                }
            }
        }
    }
}

enum class GamePhase {
    LOBBY,
    CHOOSING_TRUMP,
    PLAYING_TRICK,
    TRICK_PAUSE,
    HAND_OVER,
    MATCH_OVER
}

@JsonClass(generateAdapter = true)
data class SeatPlayer(
    val seatIndex: Int,
    val playerId: String,
    val name: String,
    val isBot: Boolean,
    val isConnected: Boolean,
    val isHost: Boolean
) {
    val teamIndex: Int get() = if (seatIndex % 2 == 0) 1 else 2
}

@JsonClass(generateAdapter = true)
data class PlayedCard(
    val seatIndex: Int,
    val card: Card
)

@JsonClass(generateAdapter = true)
data class HandResultSummary(
    val winningTeam: Int,
    val team1Tricks: Int,
    val team2Tricks: Int,
    val pointsAwarded: Int,
    val isKot: Boolean,
    val isHakemKot: Boolean,
    val nextHakemSeat: Int
)

@JsonClass(generateAdapter = true)
data class QuickChatBanner(
    val id: Long,
    val seatIndex: Int,
    val senderName: String,
    val message: String
)

@JsonClass(generateAdapter = true)
data class ClientGameSnapshot(
    val mySeat: Int,
    val players: List<SeatPlayer>,
    val phase: GamePhase,
    val targetHands: Int,
    val handNumber: Int,
    val hakemSeat: Int,
    val hakemAceCard: Card?,
    val trumpSuit: Suit?,
    val currentTurnSeat: Int,
    val trickLeadSeat: Int,
    val myHand: List<Card>,
    val handCounts: List<Int>,
    val currentTrick: List<PlayedCard>,
    val lastTrickWinnerSeat: Int?,
    val team1Tricks: Int,
    val team2Tricks: Int,
    val team1MatchScore: Int,
    val team2MatchScore: Int,
    val lastHandSummary: HandResultSummary?,
    val statusBanner: String?
)
