package com.github.discordPodcasts.wrapperJvm

import com.github.discordPodcasts.wrapperJvm.entities.Podcast
import com.github.discordPodcasts.wrapperJvm.entities.Podcasts

@Suppress("unused")
class DiscordPodcasts(
    clientId: Long,
    clientSecret: String
) {
    private val restClient = RestClient(clientId, clientSecret)

    suspend fun getPodcast(id: String): Podcast {
        return restClient.execute("/podcasts?id=${id}")
    }

    suspend fun listPodcasts(): Podcasts {
        return restClient.execute("/list")
    }

}