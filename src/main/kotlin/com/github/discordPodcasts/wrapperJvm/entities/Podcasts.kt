package com.github.discordPodcasts.wrapperJvm.entities

import kotlinx.serialization.Serializable

@Serializable
data class Podcasts(
    val podcasts: List<Podcast>
)