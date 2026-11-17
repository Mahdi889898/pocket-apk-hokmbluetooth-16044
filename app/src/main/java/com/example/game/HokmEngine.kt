package com.example.game

import kotlin.random.Random

class HokmEngine(
    private val random: Random = Random.Default
) {
    var players: MutableList<SeatPlayer> = defaultLobbyPlayers().toMutableList()
        private set

    var phase: GamePhase = GamePhase.LOBBY
        private set

    var targetHands: Int = 7
        private set

    var handNumber: Int = 0
        private set

    var hakemSeat: Int = 0
        private set

    var hakemAceCard: Card? = null
        private set

    var trumpSuit: Suit? = null
        private set

    var currentTurnSeat: Int = 0
        private set

    var trickLeadSeat: Int = 0
        private set

    private val hands: Array<MutableList<Card>> = Array(4) { mutableListOf() }
    private var remainingDeck: MutableList<Card> = mutableListOf()

    val currentTrick: MutableList<PlayedCard> = mutableListOf()
    val playedCardsInHand: MutableSet<Card> = mutableSetOf()

    var lastTrickWinnerSeat: Int? = null
        private set

    var team1Tricks: Int = 0
        private set

    var team2Tricks: Int = 0
        private set

    var team1MatchScore: Int = 0
        private set

    var team2MatchScore: Int = 0
        private set

    var lastHandSummary: HandResultSummary? = null
        private set

    var statusBanner: String? = null
        private set

    fun updateLobbyState(lobbyPlayers: List<SeatPlayer>, targetHands: Int) {
        require(lobbyPlayers.size == 4) { "Hokm requires exactly 4 seats" }
        this.players = lobbyPlayers.sortedBy { it.seatIndex }.toMutableList()
        this.targetHands = targetHands.coerceIn(1, 13)
    }

    fun startNewMatch(lobbyPlayers: List<SeatPlayer>, targetHands: Int) {
        updateLobbyState(lobbyPlayers, targetHands)
        team1MatchScore = 0
        team2MatchScore = 0
        handNumber = 0
        lastHandSummary = null

        val aceDrawDeck = Card.fullDeck().shuffled(random)
        var firstHakem = 0
        var drawnAce: Card? = null
        for ((index, card) in aceDrawDeck.withIndex()) {
            if (card.rank == Rank.ACE) {
                firstHakem = index % 4
                drawnAce = card
                break
            }
        }
        hakemSeat = firstHakem
        hakemAceCard = drawnAce
        dealFirstFiveCards(
            banner = drawnAce?.let {
                "${players[hakemSeat].name} با تک ${it.suit.persianName} حاکم اول شد"
            } ?: "${players[hakemSeat].name} حاکم اول شد"
        )
    }

    private fun dealFirstFiveCards(banner: String?) {
        handNumber += 1
        trumpSuit = null
        currentTrick.clear()
        playedCardsInHand.clear()
        lastTrickWinnerSeat = null
        team1Tricks = 0
        team2Tricks = 0
        hands.forEach { it.clear() }

        val deck = Card.fullDeck().shuffled(random).toMutableList()
        // Deal 5 cards to each player starting from Hakem
        for (offset in 0 until 4) {
            val seat = (hakemSeat + offset) % 4
            repeat(5) {
                hands[seat].add(deck.removeAt(0))
            }
            sortHand(seat, null)
        }
        remainingDeck = deck
        currentTurnSeat = hakemSeat
        trickLeadSeat = hakemSeat
        phase = GamePhase.CHOOSING_TRUMP
        statusBanner = banner
    }

    fun chooseTrump(seatIndex: Int, suit: Suit): Boolean {
        if (phase != GamePhase.CHOOSING_TRUMP || seatIndex != hakemSeat) return false
        trumpSuit = suit
        // Deal remaining 8 cards (4 + 4) starting from Hakem
        repeat(2) {
            for (offset in 0 until 4) {
                val seat = (hakemSeat + offset) % 4
                repeat(4) {
                    if (remainingDeck.isNotEmpty()) {
                        hands[seat].add(remainingDeck.removeAt(0))
                    }
                }
            }
        }
        for (seat in 0 until 4) {
            sortHand(seat, suit)
        }
        currentTurnSeat = hakemSeat
        trickLeadSeat = hakemSeat
        phase = GamePhase.PLAYING_TRICK
        statusBanner = "حکم: ${suit.symbol} ${suit.persianName}"
        return true
    }

    fun legalCardsFor(seatIndex: Int): List<Card> {
        val hand = hands[seatIndex]
        if (currentTrick.isEmpty()) return hand.toList()
        val leadSuit = currentTrick.first().card.suit
        val followSuitCards = hand.filter { it.suit == leadSuit }
        return followSuitCards.ifEmpty { hand.toList() }
    }

    fun handFor(seatIndex: Int): List<Card> = hands[seatIndex].toList()

    fun playCard(seatIndex: Int, card: Card): Boolean {
        if (phase != GamePhase.PLAYING_TRICK || seatIndex != currentTurnSeat) return false
        val legal = legalCardsFor(seatIndex)
        if (card !in legal) return false

        hands[seatIndex].remove(card)
        currentTrick.add(PlayedCard(seatIndex, card))
        playedCardsInHand.add(card)

        if (currentTrick.size < 4) {
            currentTurnSeat = (seatIndex + 1) % 4
            statusBanner = null
        } else {
            val winner = determineTrickWinner(currentTrick, trumpSuit)
            lastTrickWinnerSeat = winner
            if (winner % 2 == 0) team1Tricks++ else team2Tricks++
            phase = GamePhase.TRICK_PAUSE
            statusBanner = "${players[winner].name} این دور را بُرد"
        }
        return true
    }

    fun advanceAfterTrickPause() {
        if (phase != GamePhase.TRICK_PAUSE) return
        val winner = lastTrickWinnerSeat ?: trickLeadSeat

        if (team1Tricks >= 7 || team2Tricks >= 7) {
            val winningTeam = if (team1Tricks >= 7) 1 else 2
            val loserTricks = if (winningTeam == 1) team2Tricks else team1Tricks
            val hakemTeam = if (hakemSeat % 2 == 0) 1 else 2
            val isKot = loserTricks == 0
            val isHakemKot = isKot && (winningTeam != hakemTeam)
            val points = when {
                isHakemKot -> 3
                isKot -> 2
                else -> 1
            }

            if (winningTeam == 1) {
                team1MatchScore += points
            } else {
                team2MatchScore += points
            }

            val nextHakem = if (winningTeam == hakemTeam) hakemSeat else (hakemSeat + 1) % 4
            lastHandSummary = HandResultSummary(
                winningTeam = winningTeam,
                team1Tricks = team1Tricks,
                team2Tricks = team2Tricks,
                pointsAwarded = points,
                isKot = isKot,
                isHakemKot = isHakemKot,
                nextHakemSeat = nextHakem
            )

            currentTrick.clear()
            phase = if (team1MatchScore >= targetHands || team2MatchScore >= targetHands) {
                GamePhase.MATCH_OVER
            } else {
                GamePhase.HAND_OVER
            }
            statusBanner = when {
                isHakemKot -> "حاکم‌کُت! تیم $winningTeam با +۳ امتیاز پیروز شد"
                isKot -> "کُت! تیم $winningTeam با +۲ امتیاز پیروز شد"
                else -> "تیم $winningTeam برنده دست شد"
            }
        } else {
            currentTrick.clear()
            trickLeadSeat = winner
            currentTurnSeat = winner
            phase = GamePhase.PLAYING_TRICK
            statusBanner = null
        }
    }

    fun startNextHand() {
        if (phase != GamePhase.HAND_OVER) return
        val nextHakem = lastHandSummary?.nextHakemSeat ?: hakemSeat
        hakemSeat = nextHakem
        hakemAceCard = null
        dealFirstFiveCards(
            banner = "دست $handNumber + ۱ — حاکم: ${players[hakemSeat].name}".replace("دست $handNumber + ۱", "دست ${handNumber + 1}")
        )
    }

    fun replaceSeatWithBot(seatIndex: Int) {
        val current = players[seatIndex]
        if (!current.isHost && !current.isBot) {
            players[seatIndex] = current.copy(
                name = "${current.name} (ربات)",
                isBot = true,
                isConnected = true
            )
        }
    }

    fun updatePlayerConnection(seatIndex: Int, player: SeatPlayer) {
        players[seatIndex] = player
    }

    fun resetToLobby() {
        phase = GamePhase.LOBBY
        currentTrick.clear()
        hands.forEach { it.clear() }
        trumpSuit = null
        lastHandSummary = null
        statusBanner = null
    }

    fun snapshotForSeat(viewerSeat: Int): ClientGameSnapshot {
        return ClientGameSnapshot(
            mySeat = viewerSeat,
            players = players.toList(),
            phase = phase,
            targetHands = targetHands,
            handNumber = handNumber,
            hakemSeat = hakemSeat,
            hakemAceCard = hakemAceCard,
            trumpSuit = trumpSuit,
            currentTurnSeat = currentTurnSeat,
            trickLeadSeat = trickLeadSeat,
            myHand = hands[viewerSeat].toList(),
            handCounts = hands.map { it.size },
            currentTrick = currentTrick.toList(),
            lastTrickWinnerSeat = lastTrickWinnerSeat,
            team1Tricks = team1Tricks,
            team2Tricks = team2Tricks,
            team1MatchScore = team1MatchScore,
            team2MatchScore = team2MatchScore,
            lastHandSummary = lastHandSummary,
            statusBanner = statusBanner
        )
    }

    private fun sortHand(seat: Int, trump: Suit?) {
        val suitOrder = buildList {
            if (trump != null) add(trump)
            val remaining = listOf(Suit.SPADES, Suit.HEARTS, Suit.CLUBS, Suit.DIAMONDS)
                .filter { it != trump }
            addAll(remaining)
        }
        hands[seat].sortWith(
            compareBy<Card> { suitOrder.indexOf(it.suit) }
                .thenByDescending { it.rank.power }
        )
    }

    companion object {
        fun determineTrickWinner(trick: List<PlayedCard>, trump: Suit?): Int {
            require(trick.isNotEmpty())
            val leadSuit = trick.first().card.suit
            val trumpCards = if (trump != null) trick.filter { it.card.suit == trump } else emptyList()
            val winningPlay = if (trumpCards.isNotEmpty()) {
                trumpCards.maxBy { it.card.rank.power }
            } else {
                trick.filter { it.card.suit == leadSuit }.maxBy { it.card.rank.power }
            }
            return winningPlay.seatIndex
        }

        fun defaultLobbyPlayers(hostName: String = "شما (میزبان)"): List<SeatPlayer> = listOf(
            SeatPlayer(0, "host", hostName, isBot = false, isConnected = true, isHost = true),
            SeatPlayer(1, "bot_1", "ربات سهراب", isBot = true, isConnected = true, isHost = false),
            SeatPlayer(2, "bot_2", "ربات رستم (یار)", isBot = true, isConnected = true, isHost = false),
            SeatPlayer(3, "bot_3", "ربات کاوه", isBot = true, isConnected = true, isHost = false)
        )
    }
}
