package com.github.discordPodcasts.wrapperJvm.models.events

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = EventType.Serializer::class)
enum class EventType(val identifier: Short) {
    HELLO(0),
    DISCONNECT(1),
    UNKNOWN(-1);

    internal object Serializer : KSerializer<EventType> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("event_type", PrimitiveKind.SHORT)
        override fun deserialize(decoder: Decoder): EventType = decoder.decodeShort().let { identifier -> values().first { it.identifier == identifier } }
        override fun serialize(encoder: Encoder, value: EventType) = encoder.encodeShort(value.identifier)
    }

}
