package com.github.discordPodcasts.wrapperJvm

import kotlinx.serialization.Serializable

@Serializable
data class PodcastsEntity(
    val podcasts: List<PodcastEntity>
)