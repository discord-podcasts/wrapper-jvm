package com.github.discordPodcasts.wrapperJvm

import com.github.discordPodcasts.wrapperJvm.models.ConnectionOptions
import com.github.discordPodcasts.wrapperJvm.models.Podcast
import com.github.discordPodcasts.wrapperJvm.models.PodcastConnection
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.concurrent.ForkJoinPool

object Podcasts {
    internal var clientId = ""
    internal var token = ""
    internal val coroutineScope = CoroutineScope(ForkJoinPool.commonPool().asCoroutineDispatcher())
    private val endpoint = "podcasts.myra.bot"
    private val restEndpoint = "https://$endpoint"
    private val wsEndpoint = "wss://$endpoint"
    private val httpClient = HttpClient(CIO) {
        install(WebSockets)
        defaultRequest {
            header("clientId", clientId)
            header("token", token)
        }
    }
    internal val json = Json

    suspend fun list(): List<Podcast> {
        val req = httpClient.get("$restEndpoint/list")
        if (req.status != HttpStatusCode.OK) throw Exception(req.bodyAsText())
        else return json.decodeFromString(req.bodyAsText())
    }

    suspend fun create(): Podcast {
        val req = httpClient.post("$restEndpoint/podcast")
        if (req.status != HttpStatusCode.OK) throw Exception(req.bodyAsText())
        else return json.decodeFromString(req.bodyAsText())
    }

    suspend fun connect(podcast: Podcast, options: ConnectionOptions): PodcastConnection {
        val session = httpClient.webSocketSession(wsEndpoint) {
            parameter("id", podcast.id)
            header("isSender", options.isSender)
        }
        return PodcastConnection(session)
    }

}

fun podcasts(builder: Podcasts.() -> Unit): Podcasts = Podcasts.apply {
    apply(builder)

    if (clientId.isEmpty()) throw Exception("Missing client id")
    if (token.isEmpty()) throw Exception("Missing token")
}