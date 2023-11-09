package com.github.discordPodcasts.wrapperJvm.connection.udp

import com.codahale.xsalsa20poly1305.SecretBox
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.jvm.optionals.getOrNull

/**
 * Audio socket handling the udp connection.
 *
 * @property host Whether we're the host of the podcast.
 * @property socket The raw udp socket.
 * @property secret Secret used to encrypt/decrypt the audio data. This gets received over the [HelloEvent].
 */
class AudioSocket(
    private val host: Boolean,
    private val socket: ConnectedDatagramSocket,
    private val secret: UByteArray
) {
    val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val secretBox = SecretBox(secret.map { it.toByte() }.toByteArray())

    /**
     * If we're the host the socket doesn't provide any incoming audio. Instead,
     * we emit the data we want to send to the other clients back to the receiving flow
     * for other session to read form.
     */
    val receiver: MutableSharedFlow<ByteArray> = MutableSharedFlow()

    init {
        if (!host) {
            coroutineScope.launch {
                socket.incoming.receiveAsFlow()
                    .map { it.packet }
                    .map { secretBox.open(it.readBytes(24), it.readBytes()) }
                    .mapNotNull { it.getOrNull() }
                    .collect { receiver.emit(it) }
            }
        }
    }

    suspend fun provide(audio: ByteArray, nonce: ByteArray) {
        val cipherAudio = secretBox.seal(nonce, audio)
        val packet = BytePacketBuilder().apply {
            writeFully(nonce)
            writeFully(cipherAudio)
        }
        val datagram = Datagram(
            packet = packet.build(),
            address = socket.remoteAddress
        )

        if (host) receiver.emit(audio)
        socket.send(datagram)
    }

    fun close() {
        socket.close()
    }

}