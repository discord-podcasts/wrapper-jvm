package com.github.discordPodcasts.wrapperJvm.entities

import kotlinx.serialization.Serializable

@Serializable
data class Podcast(
    val id: Int,
    val host: String,
    val activeSince: Long?
)