package com.github.discordPodcasts.wrapperJvm.models.podcast.connection

import com.github.discordPodcasts.wrapperJvm.models.events.Event
import com.github.discordPodcasts.wrapperJvm.models.events.read.ReadEvent
import io.ktor.utils.io.core.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import org.slf4j.LoggerFactory

class Gateway(
    val socket: DefaultWebSocketSession
) {
    val logger = LoggerFactory.getLogger(this::class.java)!!
    val json = Json

    val events = socket.incoming.receiveAsFlow()
        .map { ByteReadPacket(it.readBytes()).readText() }
        .map { Json.decodeFromString<ReadEvent>(it) }
        .onEach { logger.info("Gateway << $it") }

    suspend inline fun <reified T : Event> send(event: T) {
        val eventWrapper = JsonObject(
            mutableMapOf(
                "type" to JsonPrimitive(event.type.identifier),
                "content" to json.encodeToJsonElement(event)
            )
        )
        val text = json.encodeToString(eventWrapper)
        socket.send(text)
        logger.info("Gateway >> $eventWrapper")
    }

}