package com.github.discordPodcasts.wrapperJvm

import com.github.discordPodcasts.wrapperJvm.models.podcast.connection.ConnectionOptions
import com.github.discordPodcasts.wrapperJvm.models.podcast.Podcast
import com.github.discordPodcasts.wrapperJvm.models.podcast.connection.PodcastConnection
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
    internal val coroutineScope = CoroutineScope(ForkJoinPool.commonPool().asCoroutineDispatcher())

    private const val endpoint = "podcasts.myra.bot"
    private const val restEndpoint = "https://$endpoint"
    private const val wsEndpoint = "wss://$endpoint"

    internal val json = Json
    private val httpClient = HttpClient(CIO) {
        install(WebSockets)
        defaultRequest {
            val auth = authentication ?: throw Exception("Missing authentication data")
            header("clientId", auth.clientId)
            header("token", auth.token)
        }
    }


    internal var authentication: Authentication? = null

    internal class Authentication(val clientId: String, val token: String)


    suspend fun list(): List<Podcast> {
        val req = httpClient.get("${restEndpoint}/list")
        if (req.status != HttpStatusCode.OK) throw Exception(req.bodyAsText())
        else return json.decodeFromString(req.bodyAsText())
    }

    suspend fun create(): Podcast {
        val req = httpClient.post("${restEndpoint}/podcast")
        if (req.status != HttpStatusCode.OK) throw Exception(req.bodyAsText())
        else return json.decodeFromString(req.bodyAsText())
    }


    private val listenConnections = mutableMapOf<String, PodcastConnection>()
    private val streamConnections = mutableMapOf<String, PodcastConnection>()

    fun getConnection(podcast: Podcast, options: ConnectionOptions): PodcastConnection? {
        return when (options.isSender) {
            true  -> streamConnections[podcast.id]
            false -> listenConnections[podcast.id]
        }
    }

    suspend fun createConnection(podcast: Podcast, options: ConnectionOptions): PodcastConnection {
        val session = httpClient.webSocketSession(wsEndpoint) {
            parameter("id", podcast.id)
            header("isSender", options.isSender)
        }
        val connection = PodcastConnection(session)

        when (options.isSender) {
            true  -> streamConnections[podcast.id] = connection
            false -> listenConnections[podcast.id] = connection
        }

        return connection
    }

}