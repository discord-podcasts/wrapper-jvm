package com.github.discordPodcasts.wrapperJvm.models.events

import com.github.discordPodcasts.wrapperJvm.models.events.EventType
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
open class Event(
    val type: EventType = EventType.UNKNOWN
)