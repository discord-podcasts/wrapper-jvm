package com.github.discordPodcasts.wrapperJvm

import com.github.discordPodcasts.wrapperJvm.Podcasts.endpoint
import com.github.discordPodcasts.wrapperJvm.Podcasts.httpClient
import com.github.discordPodcasts.wrapperJvm.models.ConnectionOptions
import com.github.discordPodcasts.wrapperJvm.models.Podcast
import com.github.discordPodcasts.wrapperJvm.models.PodcastConnection
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString

abstract class RestClient {
    private val restEndpoint = "https://$endpoint"
    private val wsEndpoint = "wss://$endpoint"

    suspend fun list(): List<Podcast> {
        val req = httpClient.get("${restEndpoint}/list")
        if (req.status != HttpStatusCode.OK) throw Exception(req.bodyAsText())
        else return Podcasts.json.decodeFromString(req.bodyAsText())
    }

    suspend fun create(): Podcast {
        val req = httpClient.post("${restEndpoint}/podcast")
        if (req.status != HttpStatusCode.OK) throw Exception(req.bodyAsText())
        else return Podcasts.json.decodeFromString(req.bodyAsText())
    }

    suspend fun connect(podcast: Podcast, options: ConnectionOptions): PodcastConnection {
        val session = httpClient.webSocketSession(wsEndpoint) {
            parameter("id", podcast.id)
            header("isSender", options.isSender)
        }
        return PodcastConnection(session)
    }

}