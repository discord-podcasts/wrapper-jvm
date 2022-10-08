package com.github.discordPodcasts.wrapperJvm.models.podcast.connection

import com.github.discordPodcasts.wrapperJvm.Podcasts
import com.github.discordPodcasts.wrapperJvm.models.events.Event
import com.github.discordPodcasts.wrapperJvm.models.events.EventType
import com.github.discordPodcasts.wrapperJvm.models.events.HelloEvent
import com.github.discordPodcasts.wrapperJvm.models.packets.PacketType
import io.ktor.client.plugins.websocket.*
import io.ktor.utils.io.core.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.time.Duration.Companion.seconds

class PodcastConnection(
    private val socketSession: DefaultClientWebSocketSession
) {
    private val audioManager = AudioConnectionManager(socketSession)
    var localeParticipants = 0

    private val eventFlow: Flow<Event> = socketSession.incoming.receiveAsFlow()
        .map { ByteReadPacket(it.data) }
        .filter { PacketType.fromCode(it.readByte()) == PacketType.EVENT }
        .map { Podcasts.json.decodeFromString(it.readText()) }

    init {
        // Wait for hello event
        Podcasts.coroutineScope.launch {
            withTimeout(5.seconds) {
                val helloEventRaw = eventFlow.first { it.type == EventType.HELLO }
                val helloEvent = Podcasts.json.decodeFromJsonElement<HelloEvent>(helloEventRaw.content)
                audioManager.setSecret(helloEvent.secretKey.toByteArray())
            }
        }
    }

    suspend fun disconnect() {
        localeParticipants--
        if (localeParticipants == 0) socketSession.close(CloseReason(1000, "Closed after user request"))
    }

    fun getAudioManager(): AudioConnectionManager {
        localeParticipants++
        return audioManager
    }

}