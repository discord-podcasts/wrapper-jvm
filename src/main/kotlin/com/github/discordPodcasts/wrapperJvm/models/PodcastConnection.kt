package com.github.discordPodcasts.wrapperJvm.models

import com.codahale.xsalsa20poly1305.SecretBox
import com.github.discordPodcasts.wrapperJvm.Podcasts
import com.github.discordPodcasts.wrapperJvm.models.events.Event
import com.github.discordPodcasts.wrapperJvm.models.events.EventType
import com.github.discordPodcasts.wrapperJvm.models.events.HelloEvent
import com.github.discordPodcasts.wrapperJvm.models.packets.AudioPacket
import com.github.discordPodcasts.wrapperJvm.models.packets.PacketType
import io.ktor.client.plugins.websocket.*
import io.ktor.utils.io.core.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration.Companion.seconds

class PodcastConnection(private val socketSession: DefaultClientWebSocketSession) {
    private var secretBox: SecretBox? = null

    private val eventFlow: Flow<Event> = socketSession.incoming.receiveAsFlow()
        .map { ByteReadPacket(it.data) }
        .filter { PacketType.fromCode(it.readByte()) == PacketType.EVENT }
        .map { Podcasts.json.decodeFromString(it.readText()) }

    private val audioFlow: Flow<AudioPacket> = socketSession.incoming.receiveAsFlow()
        .map { ByteReadPacket(it.data) }
        .filter { PacketType.fromCode(it.readByte()) == PacketType.AUDIO }
        .map { AudioPacket.fromPacket(it) }

    init {
        // Wait for hello event
        Podcasts.coroutineScope.launch {
            withTimeout(5.seconds) {
                val helloEventRaw = eventFlow.first { it.type == EventType.HELLO }
                val helloEvent = Podcasts.json.decodeFromJsonElement<HelloEvent>(helloEventRaw.content)
                secretBox = SecretBox(helloEvent.secretKey.toByteArray())
            }
        }
    }

    suspend fun disconnect() {
        socketSession.close(CloseReason(1000, "Closed after user request"))
    }

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