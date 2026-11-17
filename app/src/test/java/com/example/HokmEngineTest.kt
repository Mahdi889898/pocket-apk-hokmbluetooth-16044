package com.example

import com.example.game.Card
import com.example.game.GamePhase
import com.example.game.HokmBotAi
import com.example.game.HokmEngine
import com.example.game.PlayedCard
import com.example.game.Rank
import com.example.game.Suit
import com.example.network.ChooseTrumpPayload
import com.example.network.HokmJsonCodec
import com.example.network.JoinRequestPayload
import com.example.network.MessageType
import com.example.network.PlayCardPayload
import com.example.network.QuickChatPayload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class HokmEngineTest {

    @Test
    fun `startNewMatch deals 5 cards each and transitions to CHOOSING_TRUMP`() {
        val engine = HokmEngine(Random(42))
        val players = HokmEngine.defaultLobbyPlayers("آرش")
        engine.startNewMatch(players, targetHands = 7)

        assertEquals(GamePhase.CHOOSING_TRUMP, engine.phase)
        for (seat in 0 until 4) {
            assertEquals(5, engine.handFor(seat).size)
        }
        assertEquals(engine.hakemSeat, engine.currentTurnSeat)
    }

    @Test
    fun `chooseTrump deals remaining 8 cards for 52 unique cards across 4 seats`() {
        val engine = HokmEngine(Random(99))
        engine.startNewMatch(HokmEngine.defaultLobbyPlayers("سارا"), targetHands = 7)

        val hakem = engine.hakemSeat
        assertFalse(engine.chooseTrump((hakem + 1) % 4, Suit.HEARTS))
        assertTrue(engine.chooseTrump(hakem, Suit.HEARTS))
        assertEquals(GamePhase.PLAYING_TRICK, engine.phase)
        assertEquals(Suit.HEARTS, engine.trumpSuit)

        val allDealt = (0 until 4).flatMap { engine.handFor(it) }
        assertEquals(52, allDealt.size)
        assertEquals(52, allDealt.toSet().size)
    }

    @Test
    fun `determineTrickWinner respects trump over higher led suit`() {
        val trick = listOf(
            PlayedCard(0, Card(Suit.SPADES, Rank.ACE)),
            PlayedCard(1, Card(Suit.SPADES, Rank.KING)),
            PlayedCard(2, Card(Suit.HEARTS, Rank.TWO)),
            PlayedCard(3, Card(Suit.CLUBS, Rank.ACE))
        )
        val winner = HokmEngine.determineTrickWinner(trick, Suit.HEARTS)
        assertEquals(2, winner)
    }

    @Test
    fun `full simulated hand completes at 7 tricks and awards valid points`() {
        val engine = HokmEngine(Random(1234))
        engine.startNewMatch(HokmEngine.defaultLobbyPlayers("میزبان"), targetHands = 7)
        val hakem = engine.hakemSeat
        val chosenTrump = HokmBotAi.chooseTrump(engine.handFor(hakem))
        assertTrue(engine.chooseTrump(hakem, chosenTrump))

        var safetySteps = 0
        while (engine.phase == GamePhase.PLAYING_TRICK || engine.phase == GamePhase.TRICK_PAUSE) {
            if (engine.phase == GamePhase.TRICK_PAUSE) {
                engine.advanceAfterTrickPause()
            } else {
                val seat = engine.currentTurnSeat
                val card = HokmBotAi.chooseCard(
                    hand = engine.handFor(seat),
                    legalCards = engine.legalCardsFor(seat),
                    currentTrick = engine.currentTrick,
                    trumpSuit = engine.trumpSuit!!,
                    mySeat = seat
                )
                assertTrue(engine.playCard(seat, card))
            }
            safetySteps++
            assertTrue(safetySteps < 100)
        }

        assertTrue(engine.phase == GamePhase.HAND_OVER || engine.phase == GamePhase.MATCH_OVER)
        val summary = engine.lastHandSummary
        assertNotNull(summary)
        assertTrue(summary!!.team1Tricks == 7 || summary.team2Tricks == 7)
        assertTrue(summary.pointsAwarded in 1..3)
    }

    @Test
    fun `moshi network codec round trips snapshot and action envelopes`() {
        val engine = HokmEngine(Random(7))
        engine.startNewMatch(HokmEngine.defaultLobbyPlayers("رضا"), targetHands = 5)
        engine.chooseTrump(engine.hakemSeat, Suit.DIAMONDS)
        val snap = engine.snapshotForSeat(1)

        val encodedSnap = HokmJsonCodec.encodeSnapshot(snap)
        val env = HokmJsonCodec.decodeEnvelope(encodedSnap)
        assertNotNull(env)
        assertEquals(MessageType.SNAPSHOT, env!!.type)
        val decodedSnap = HokmJsonCodec.decodeSnapshot(env.payload)
        assertEquals(snap, decodedSnap)

        val joinLine = HokmJsonCodec.encodeJoin(JoinRequestPayload("id-1", "مریم"))
        val joinEnv = HokmJsonCodec.decodeEnvelope(joinLine)!!
        assertEquals("مریم", HokmJsonCodec.decodeJoin(joinEnv.payload)?.playerName)

        val trumpLine = HokmJsonCodec.encodeChooseTrump(ChooseTrumpPayload(Suit.SPADES.name))
        val trumpEnv = HokmJsonCodec.decodeEnvelope(trumpLine)!!
        assertEquals(Suit.SPADES.name, HokmJsonCodec.decodeChooseTrump(trumpEnv.payload)?.suitName)

        val playLine = HokmJsonCodec.encodePlayCard(PlayCardPayload(Suit.HEARTS.name, Rank.ACE.name))
        val playEnv = HokmJsonCodec.decodeEnvelope(playLine)!!
        assertEquals(Rank.ACE.name, HokmJsonCodec.decodePlayCard(playEnv.payload)?.rankName)

        val chatLine = HokmJsonCodec.encodeQuickChat(QuickChatPayload(2, "سارا", "بزن!"))
        val chatEnv = HokmJsonCodec.decodeEnvelope(chatLine)!!
        assertEquals("بزن!", HokmJsonCodec.decodeQuickChat(chatEnv.payload)?.message)
    }
}
