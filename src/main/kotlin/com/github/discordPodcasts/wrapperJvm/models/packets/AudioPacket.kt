package com.github.discordPodcasts.wrapperJvm.models.packets

import io.ktor.utils.io.core.*

data class AudioPacket(
    val nonce: ByteArray,
    val audio: ByteArray
) {
    companion object {
        fun fromPacket(rawPacket: ByteReadPacket): AudioPacket = AudioPacket(
            nonce = rawPacket.readBytes(24),
            audio = rawPacket.readBytes()
        )
    }

}