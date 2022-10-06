package com.github.discordPodcasts.wrapperJvm.models

import com.github.discordPodcasts.wrapperJvm.Podcasts
import kotlinx.serialization.Serializable

@Serializable
data class Podcast(
    val id: String,
    val activeSince: Boolean
) {

    suspend fun connect(options: ConnectionOptions.() -> Unit) {
        Podcasts.connect(this, ConnectionOptions().apply(options))
    }

}