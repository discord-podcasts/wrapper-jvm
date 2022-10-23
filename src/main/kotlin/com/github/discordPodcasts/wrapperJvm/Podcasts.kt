package com.github.discordPodcasts.wrapperJvm

import com.github.discordPodcasts.wrapperJvm.models.podcast.Podcast
import com.github.discordPodcasts.wrapperJvm.models.podcast.connection.ConnectionOptions
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
import org.slf4j.LoggerFactory
import java.util.concurrent.ForkJoinPool

object Podcasts {
    internal val coroutineScope = CoroutineScope(ForkJoinPool.commonPool().asCoroutineDispatcher())

    private const val endpoint = "127.0.0.1:5050"
    private const val restEndpoint = "http://$endpoint"
    private const val wsEndpoint = "ws://$endpoint"

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

    suspend fun getRemoteIp(): String {
        val req = httpClient.get("${restEndpoint}/ip")
        if (req.status != HttpStatusCode.OK) throw Exception(req.bodyAsText())
        else return req.bodyAsText()
    }

    suspend fun list(): List<Podcast> {
        val req = httpClient.get("${restEndpoint}/list")
        if (req.status != HttpStatusCode.OK) throw Exception(req.bodyAsText())
        else return json.decodeFromString(req.bodyAsText())
    }

    suspend fun create(): Podcast {
        val req = httpClient.post("${restEndpoint}/podcast")
        println(req.bodyAsText())
        if (req.status != HttpStatusCode.OK) throw Exception(req.bodyAsText())
        else return json.decodeFromString(req.bodyAsText())
    }


    private val listenConnections = mutableMapOf<String, PodcastConnection>()
    private val streamConnections = mutableMapOf<String, PodcastConnection>()

    fun getConnection(podcast: Podcast, options: ConnectionOptions): PodcastConnection? {
        return when (options.host) {
            true  -> streamConnections[podcast.id]
            false -> listenConnections[podcast.id]
        }
    }

    suspend fun createConnection(podcast: Podcast, options: ConnectionOptions): PodcastConnection {
        LoggerFactory.getLogger(this::class.java).info("Opening gateway connection")

        val client = HttpClient(CIO) { install(WebSockets) }

        val socketSession = client.webSocketSession("$wsEndpoint") {
            val auth = authentication ?: throw Exception("Missing authentication data")
            header("clientId", auth.clientId)
            header("token", auth.token)
            parameter("id", podcast.id)
        }
        val connection = PodcastConnection(socketSession, options.host)

        when (options.host) {
            true  -> streamConnections[podcast.id] = connection
            false -> listenConnections[podcast.id] = connection
        }

        return connection
    }

}