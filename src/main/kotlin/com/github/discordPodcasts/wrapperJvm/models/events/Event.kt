package com.github.discordPodcasts.wrapperJvm.models.events

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
abstract class Event(
    val type: EventType,
    val content: JsonObject
)
