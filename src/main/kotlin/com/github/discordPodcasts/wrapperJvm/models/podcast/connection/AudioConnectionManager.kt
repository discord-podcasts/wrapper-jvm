package com.github.discordPodcasts.wrapperJvm.models.podcast.connection

import com.codahale.xsalsa20poly1305.SecretBox
import com.github.discordPodcasts.wrapperJvm.models.packets.AudioPacket
import com.github.discordPodcasts.wrapperJvm.models.packets.PacketType
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import kotlin.jvm.optionals.getOrNull

class AudioConnectionManager(
    private val remoteAddress: InetSocketAddress,
    secretKey: List<Byte>,
    private val host: Boolean
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    val socket = aSocket(SelectorManager(Dispatchers.IO)).udp().connect(remoteAddress)
    val address = socket.localAddress
    private var secretBox: SecretBox = SecretBox(secretKey.toByteArray())

    private val audioFlow: Flow<AudioPacket> = socket.incoming.receiveAsFlow()
        .map { it.packet }
        .filter { PacketType.fromCode(it.readByte()) == PacketType.AUDIO }
        .map { AudioPacket.fromPacket(it) }

    init {
        logger.info("Created udp socket at " + socket.remoteAddress)
    }

    suspend fun provideAudio(encryptedAudio: ByteArray, nonce: ByteArray) {
        if (!host) throw UnsupportedOperationException("A non-host can't provide audio")

        val decryptedAudio = secretBox.seal(nonce, encryptedAudio)
        val packet = BytePacketBuilder().apply {
            writeFully(nonce)
            writeFully(decryptedAudio)
        }

        val datagram = Datagram(
            packet = packet.build(),
            address = remoteAddress
        )
        println("Sent: " + datagram.packet.copy().readBytes().toList())
        socket.send(datagram)
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun receiveAudio(): Flow<ByteArray> = audioFlow.mapNotNull {
        secretBox.open(it.nonce, it.audio)?.getOrNull()
    }

}