package com.github.discordPodcasts.wrapperJvm.connection.gateway

import com.github.discordPodcasts.wrapperJvm.DiscordPodcasts
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.decodeFromJsonElement
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class Gateway(
    private var websocket: DefaultClientWebSocketSession,
    val api: DiscordPodcasts
) {
    private val logger = LoggerFactory.getLogger("PodcastGateway")
    val events = websocket.incoming.receiveAsFlow()
        .map { String(it.data) }
        .map { api.json.decodeFromString<Event>(it) }
        .onEach { if (logger.isEnabledForLevel(Level.DEBUG)) logger.debug("<< $it") }

    suspend inline fun <reified T> awaitEvent(type: EventType, timeout: Duration = 4.seconds): T? {
        return withTimeoutOrNull(timeout) {
            events.first { it.type === type }.let { api.json.decodeFromJsonElement<T>(it.data) }
        }
    }

    suspend fun send(event: Event) {
        val json = api.json.encodeToString(event)
        websocket.send(json)
        logger.debug(">> $event")
    }

    suspend fun close() {
        websocket.close()
    }

}