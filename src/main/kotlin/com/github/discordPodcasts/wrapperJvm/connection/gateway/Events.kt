package com.github.discordPodcasts.wrapperJvm.connection.gateway

import com.github.discordPodcasts.wrapperJvm.DiscordPodcasts
import io.ktor.network.sockets.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

@Serializable(with = EventType.Serializer::class)
enum class EventType(val code: Int) {
    HELLO(1),
    CONNECTED(2),
    DISCONNECTED(3),
    CLIENT_JOIN(4),
    END(5);

    internal class Serializer : KSerializer<EventType> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("event_type", PrimitiveKind.INT)
        override fun deserialize(decoder: Decoder): EventType = decoder.decodeInt().let { code -> values().first { it.code == code } }
        override fun serialize(encoder: Encoder, value: EventType) = encoder.encodeInt(value.code)
    }
}

@Serializable
data class Event(
    @SerialName("t") val type: EventType,
    val data: JsonElement
)

@Serializable
data class HelloEvent(
    @SerialName("podcast_id") val podcastId: UInt,
    val ip: String,
    val port: Int,
    val secret: UByteArray
) {
    @Contextual
    val address = InetSocketAddress(ip, port)
}

@Serializable
data class ConnectedEvent(
    val ip: String,
    val port: Int
) {
    fun prepare(api: DiscordPodcasts): Event =
        Event(EventType.CONNECTED, api.json.encodeToJsonElement(this))
}