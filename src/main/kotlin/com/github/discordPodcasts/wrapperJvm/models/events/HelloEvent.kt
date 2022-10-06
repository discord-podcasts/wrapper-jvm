package com.github.discordPodcasts.wrapperJvm.models.events

import kotlinx.serialization.Serializable

@Serializable
data class HelloEvent(
    val secretKey: List<Byte>
)
