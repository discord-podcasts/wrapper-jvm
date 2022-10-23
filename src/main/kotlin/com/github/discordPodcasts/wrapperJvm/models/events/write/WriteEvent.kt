package com.github.discordPodcasts.wrapperJvm.models.events.write

import com.github.discordPodcasts.wrapperJvm.models.events.EventType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class WriteEvent(
    val type: EventType,
    val content: JsonObject
)