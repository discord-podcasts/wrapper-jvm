package com.github.discordPodcasts.wrapperJvm

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PodcastEntity(
    val id: UInt,
    val host: ULong,
    @SerialName("active_since") val activeSince: Long?
)

class Podcast(
    val entity: PodcastEntity,
    val api: DiscordPodcasts
)