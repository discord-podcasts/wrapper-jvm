package com.github.discordPodcasts.wrapperJvm.models.podcast

import com.github.discordPodcasts.wrapperJvm.Podcasts
import com.github.discordPodcasts.wrapperJvm.models.podcast.connection.ConnectionOptions
import com.github.discordPodcasts.wrapperJvm.models.podcast.connection.PodcastConnection
import kotlinx.serialization.Serializable

@Serializable
data class Podcast(
    val id: String,
    val activeSince: Long?
) {

    fun getConnectionOrNull(options: ConnectionOptions.() -> Unit = {}): PodcastConnection? {
        val options = ConnectionOptions().apply(options)
        return Podcasts.getConnection(this, options)
    }

    suspend fun getConnectionOrCreate(options: ConnectionOptions.() -> Unit = {}): PodcastConnection {
        val options = ConnectionOptions().apply(options)
        return Podcasts.getConnection(this, options)
            ?: Podcasts.createConnection(this, options)
    }

}