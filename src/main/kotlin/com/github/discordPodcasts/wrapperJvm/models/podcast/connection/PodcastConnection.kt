package com.github.discordPodcasts.wrapperJvm.models.podcast.connection

import com.github.discordPodcasts.wrapperJvm.models.events.Event
import com.github.discordPodcasts.wrapperJvm.Podcasts
import com.github.discordPodcasts.wrapperJvm.models.events.EventType
import com.github.discordPodcasts.wrapperJvm.models.events.read.HelloEvent
import com.github.discordPodcasts.wrapperJvm.models.events.write.IdentifyEvent
import io.ktor.network.sockets.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

class PodcastConnection(
    socketSession: DefaultWebSocketSession,
    private val host: Boolean
) {
    private val gateway = Gateway(socketSession)
    var audioManager: AudioConnectionManager? = null

    suspend fun establishAudioConnection(): AudioConnectionManager {
        val auth = Podcasts.authentication ?: throw Exception("Missing authentication data")


        val helloEvent = awaitEvent<HelloEvent>(EventType.HELLO)

        val audioManager = AudioConnectionManager(helloEvent.remoteAddress, helloEvent.secretKey, host)
        this.audioManager = audioManager
        val address = audioManager.address as InetSocketAddress
        val identifyEvent = IdentifyEvent(address.hostname, address.port)
        gateway.send(identifyEvent)

        return audioManager
    }

    private suspend inline fun <reified T : Event> awaitEvent(type: EventType): T = withTimeoutOrNull(2.seconds) {
        gateway.events
            .filter { it.type == EventType.HELLO }
            .map { it.interpret<T>() }
            .first()
    } ?: throw Exception("Missing ${type.name} event after timeout")

    /*
    private val eventFlow: Flow<Event> = socketSession.incoming.receiveAsFlow()
        .map { ByteReadPacket(it.data) }
        .filter { PacketType.fromCode(it.readByte()) == PacketType.EVENT }
        .map { Podcasts.json.decodeFromString(it.readText()) }
     */

    init {
        // Wait for hello event
        /*
        Podcasts.coroutineScope.launch {
            withTimeout(5.seconds) {
                val helloEventRaw = eventFlow.first { it.type == EventType.HELLO }
                val helloEvent = Podcasts.json.decodeFromJsonElement<HelloEvent>(helloEventRaw.content)
                audioManager.setSecret(helloEvent.secretKey.toByteArray())
            }
        }*/
    }

}