package com.example.game

object HokmBotAi {

    fun chooseTrump(fiveCards: List<Card>): Suit {
        return Suit.entries.maxBy { suit ->
            val ofSuit = fiveCards.filter { it.suit == suit }
            val countScore = ofSuit.size * 6.0
            val powerScore = ofSuit.sumOf { card ->
                when (card.rank) {
                    Rank.ACE -> 7.0
                    Rank.KING -> 5.0
                    Rank.QUEEN -> 3.5
                    Rank.JACK -> 2.0
                    Rank.TEN -> 1.2
                    else -> 0.4
                }
            }
            countScore + powerScore
        }
    }

    fun chooseCard(
        hand: List<Card>,
        legalCards: List<Card>,
        currentTrick: List<PlayedCard>,
        trumpSuit: Suit,
        mySeat: Int
    ): Card {
        if (legalCards.size == 1) return legalCards.first()

        // Leading the trick
        if (currentTrick.isEmpty()) {
            val nonTrumpAces = legalCards.filter { it.suit != trumpSuit && it.rank == Rank.ACE }
            if (nonTrumpAces.isNotEmpty()) return nonTrumpAces.first()

            val nonTrumpKings = legalCards.filter { it.suit != trumpSuit && it.rank == Rank.KING }
            if (nonTrumpKings.isNotEmpty()) {
                // Lead K if we also hold Q or several of that suit, or just strong suit
                val bestKing = nonTrumpKings.maxBy { king ->
                    hand.count { it.suit == king.suit }
                }
                return bestKing
            }

            // If holding 5+ trumps with Ace, draw trumps
            val myTrumps = legalCards.filter { it.suit == trumpSuit }
            if (myTrumps.size >= 4 && myTrumps.any { it.rank == Rank.ACE }) {
                return myTrumps.maxBy { it.rank.power }
            }

            // Otherwise lead lowest card from shortest non-trump suit to create a void
            val nonTrumps = legalCards.filter { it.suit != trumpSuit }
            if (nonTrumps.isNotEmpty()) {
                return nonTrumps.minWith(
                    compareBy<Card> { card -> hand.count { it.suit == card.suit } }
                        .thenBy { it.rank.power }
                )
            }
            return legalCards.minBy { it.rank.power }
        }

        val leadSuit = currentTrick.first().card.suit
        val currentWinnerSeat = HokmEngine.determineTrickWinner(currentTrick, trumpSuit)
        val partnerSeat = (mySeat + 2) % 4
        val partnerCurrentlyWinning = currentWinnerSeat == partnerSeat

        val currentWinningCard = currentTrick.first { it.seatIndex == currentWinnerSeat }.card
        val isLastToPlay = currentTrick.size == 3

        val winningCandidates = legalCards.filter { candidate ->
            beats(candidate, currentWinningCard, leadSuit, trumpSuit)
        }

        if (partnerCurrentlyWinning) {
            // If last to play, or partner holds an Ace or trump King/Ace, slough lowest card
            val partnerSafe = isLastToPlay ||
                currentWinningCard.rank == Rank.ACE ||
                (currentWinningCard.suit == trumpSuit && currentWinningCard.rank.power >= Rank.QUEEN.power)
            if (partnerSafe) {
                return sloughLowest(legalCards, trumpSuit)
            }
            // Otherwise if we can overtake cheaply with a non-trump Ace/King, take it; else slough
            val cheapNonTrumpWin = winningCandidates
                .filter { it.suit != trumpSuit }
                .minByOrNull { it.rank.power }
            if (cheapNonTrumpWin != null && cheapNonTrumpWin.rank == Rank.ACE) {
                return cheapNonTrumpWin
            }
            return sloughLowest(legalCards, trumpSuit)
        }

        // Opponent is currently winning
        if (winningCandidates.isNotEmpty()) {
            val nonTrumpWins = winningCandidates.filter { it.suit != trumpSuit }
            if (nonTrumpWins.isNotEmpty()) {
                return if (isLastToPlay) {
                    nonTrumpWins.minBy { it.rank.power }
                } else {
                    nonTrumpWins.maxBy { it.rank.power }
                }
            }
            // Cut with lowest winning trump card
            return winningCandidates.minBy { it.rank.power }
        }

        // Cannot win the trick -> throw lowest non-trump card
        return sloughLowest(legalCards, trumpSuit)
    }

    private fun sloughLowest(legalCards: List<Card>, trumpSuit: Suit): Card {
        val nonTrumps = legalCards.filter { it.suit != trumpSuit }
        return (nonTrumps.ifEmpty { legalCards }).minBy { it.rank.power }
    }

    private fun beats(
        challenger: Card,
        incumbent: Card,
        leadSuit: Suit,
        trumpSuit: Suit
    ): Boolean {
        return when {
            challenger.suit == trumpSuit && incumbent.suit != trumpSuit -> true
            challenger.suit != trumpSuit && incumbent.suit == trumpSuit -> false
            challenger.suit == incumbent.suit -> challenger.rank.power > incumbent.rank.power
            challenger.suit == leadSuit && incumbent.suit != leadSuit && incumbent.suit != trumpSuit -> true
            else -> false
        }
    }
}
