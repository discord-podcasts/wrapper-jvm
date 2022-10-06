package com.github.discordPodcasts.wrapperJvm.models.packets

import io.ktor.utils.io.core.*

data class AudioPacket(
    val nonce: ByteArray,
    val audio: ByteArray
) {
    companion object {
        fun fromPacket(rawPacket: ByteReadPacket): AudioPacket {
            if (PacketType.fromCode(rawPacket.readByte()) != PacketType.AUDIO) throw Exception("Packet is no audio packet")

            return AudioPacket(
                nonce = rawPacket.readBytes(24),
                audio = rawPacket.readBytes()
            )
        }
    }

}