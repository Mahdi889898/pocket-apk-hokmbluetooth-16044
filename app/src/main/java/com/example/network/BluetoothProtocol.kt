package com.example.network

import com.example.game.ClientGameSnapshot
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object MessageType {
    const val JOIN_REQUEST = "JOIN_REQUEST"
    const val SNAPSHOT = "SNAPSHOT"
    const val CHOOSE_TRUMP = "CHOOSE_TRUMP"
    const val PLAY_CARD = "PLAY_CARD"
    const val QUICK_CHAT = "QUICK_CHAT"
    const val ROOM_ERROR = "ROOM_ERROR"
}

@JsonClass(generateAdapter = true)
data class NetworkEnvelope(
    val type: String,
    val payload: String
)

@JsonClass(generateAdapter = true)
data class JoinRequestPayload(
    val playerId: String,
    val playerName: String
)

@JsonClass(generateAdapter = true)
data class ChooseTrumpPayload(
    val suitName: String
)

@JsonClass(generateAdapter = true)
data class PlayCardPayload(
    val suitName: String,
    val rankName: String
)

@JsonClass(generateAdapter = true)
data class QuickChatPayload(
    val seatIndex: Int,
    val senderName: String,
    val message: String
)

@JsonClass(generateAdapter = true)
data class RoomErrorPayload(
    val message: String
)

object HokmJsonCodec {
    val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val envelopeAdapter = moshi.adapter(NetworkEnvelope::class.java)
    private val joinAdapter = moshi.adapter(JoinRequestPayload::class.java)
    private val snapshotAdapter = moshi.adapter(ClientGameSnapshot::class.java)
    private val chooseTrumpAdapter = moshi.adapter(ChooseTrumpPayload::class.java)
    private val playCardAdapter = moshi.adapter(PlayCardPayload::class.java)
    private val quickChatAdapter = moshi.adapter(QuickChatPayload::class.java)
    private val errorAdapter = moshi.adapter(RoomErrorPayload::class.java)

    fun encodeJoin(payload: JoinRequestPayload): String =
        envelopeAdapter.toJson(NetworkEnvelope(MessageType.JOIN_REQUEST, joinAdapter.toJson(payload)))

    fun encodeSnapshot(snapshot: ClientGameSnapshot): String =
        envelopeAdapter.toJson(NetworkEnvelope(MessageType.SNAPSHOT, snapshotAdapter.toJson(snapshot)))

    fun encodeChooseTrump(payload: ChooseTrumpPayload): String =
        envelopeAdapter.toJson(NetworkEnvelope(MessageType.CHOOSE_TRUMP, chooseTrumpAdapter.toJson(payload)))

    fun encodePlayCard(payload: PlayCardPayload): String =
        envelopeAdapter.toJson(NetworkEnvelope(MessageType.PLAY_CARD, playCardAdapter.toJson(payload)))

    fun encodeQuickChat(payload: QuickChatPayload): String =
        envelopeAdapter.toJson(NetworkEnvelope(MessageType.QUICK_CHAT, quickChatAdapter.toJson(payload)))

    fun encodeError(payload: RoomErrorPayload): String =
        envelopeAdapter.toJson(NetworkEnvelope(MessageType.ROOM_ERROR, errorAdapter.toJson(payload)))

    fun decodeEnvelope(line: String): NetworkEnvelope? = runCatching {
        envelopeAdapter.fromJson(line)
    }.getOrNull()

    fun decodeJoin(json: String): JoinRequestPayload? = runCatching { joinAdapter.fromJson(json) }.getOrNull()
    fun decodeSnapshot(json: String): ClientGameSnapshot? = runCatching { snapshotAdapter.fromJson(json) }.getOrNull()
    fun decodeChooseTrump(json: String): ChooseTrumpPayload? = runCatching { chooseTrumpAdapter.fromJson(json) }.getOrNull()
    fun decodePlayCard(json: String): PlayCardPayload? = runCatching { playCardAdapter.fromJson(json) }.getOrNull()
    fun decodeQuickChat(json: String): QuickChatPayload? = runCatching { quickChatAdapter.fromJson(json) }.getOrNull()
    fun decodeError(json: String): RoomErrorPayload? = runCatching { errorAdapter.fromJson(json) }.getOrNull()
}
