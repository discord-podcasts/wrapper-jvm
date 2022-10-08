package com.github.discordPodcasts.wrapperJvm.models.podcast.connection

import com.codahale.xsalsa20poly1305.SecretBox
import com.github.discordPodcasts.wrapperJvm.models.packets.AudioPacket
import com.github.discordPodcasts.wrapperJvm.models.packets.PacketType
import io.ktor.client.plugins.websocket.*
import io.ktor.utils.io.core.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.*
import kotlin.jvm.optionals.getOrNull

class AudioConnectionManager(
    private val socketSession: DefaultClientWebSocketSession
) {
    private var secretBox: SecretBox? = null
    internal fun setSecret(secret: ByteArray) = run { secretBox = SecretBox(secret) }

    private val audioFlow: Flow<AudioPacket> = socketSession.incoming.receiveAsFlow()
        .map { ByteReadPacket(it.data) }
        .filter { PacketType.fromCode(it.readByte()) == PacketType.AUDIO }
        .map { AudioPacket.fromPacket(it) }

    suspend fun provideAudio(encryptedAudio: ByteArray, nonce: ByteArray) {
        if (secretBox == null) return
        val decryptedAudio = secretBox!!.seal(nonce, encryptedAudio)
        val packet = BytePacketBuilder().apply {
            writeByte(PacketType.AUDIO.raw)
            writeFully(nonce)
            writeFully(decryptedAudio)
        }
        socketSession.send(packet.build().readBytes())
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun receiveAudio(): Flow<ByteArray> = audioFlow.mapNotNull {
        secretBox?.open(it.nonce, it.audio)?.getOrNull()
    }

}