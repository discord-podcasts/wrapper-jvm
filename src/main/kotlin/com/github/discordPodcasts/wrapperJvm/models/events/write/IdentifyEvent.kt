package com.github.discordPodcasts.wrapperJvm.models.events.write

import com.github.discordPodcasts.wrapperJvm.models.events.Event
import com.github.discordPodcasts.wrapperJvm.models.events.EventType
import kotlinx.serialization.Serializable

@Serializable
data class IdentifyEvent(
    val ip: String,
    val port: Int
) : Event(EventType.FIND_ADDRESS)