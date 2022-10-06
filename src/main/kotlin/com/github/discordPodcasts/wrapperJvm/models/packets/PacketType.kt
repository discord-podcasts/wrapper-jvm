package com.github.discordPodcasts.wrapperJvm.models.packets

enum class PacketType(val raw: Byte) {
    EVENT(0),
    AUDIO(1);

    companion object {
        fun fromCode(byte: Byte): PacketType = values().first { it.raw == byte }
    }
}