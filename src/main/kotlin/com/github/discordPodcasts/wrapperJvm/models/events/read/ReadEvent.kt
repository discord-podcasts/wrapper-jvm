package com.github.discordPodcasts.wrapperJvm.models.events.read

import com.github.discordPodcasts.wrapperJvm.models.events.Event
import com.github.discordPodcasts.wrapperJvm.models.events.EventType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

@Serializable
data class ReadEvent(
    val type: EventType,
    val content: JsonObject
) {
    inline fun <reified T : Event> interpret(): T {
        return Json.decodeFromJsonElement(content)
    }
}