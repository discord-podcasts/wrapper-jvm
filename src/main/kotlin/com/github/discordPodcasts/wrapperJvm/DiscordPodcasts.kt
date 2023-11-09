package com.github.discordPodcasts.wrapperJvm

import com.github.discordPodcasts.wrapperJvm.connection.PodcastConnection
import com.github.discordPodcasts.wrapperJvm.connection.gateway.*
import com.github.discordPodcasts.wrapperJvm.connection.udp.AudioSocket
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Suppress("unused")
class DiscordPodcasts(
    private val clientId: Long,
    private val clientSecret: String
) {
    private val restUrl = "https://podcasts.myra.bot"
    private val websocketUrl = "wss://podcasts.myra.bot"
    private val httpClient = HttpClient(CIO) {
        install(WebSockets)
        defaultRequest {
            header("client_id", clientId)
            header("client_secret", clientSecret)
        }
    }
    val json = Json
    private val connections = mutableMapOf<UInt, PodcastConnection>()

    suspend fun getPodcast(id: String): Podcast? {
        val result = httpClient.get("$restUrl/podcast?id=$id")
        return if (result.status.isSuccess()) {
            val body = result.bodyAsText()
            val entity = json.decodeFromString<PodcastEntity>(body)
            Podcast(entity, this)
        } else null
    }

    suspend fun listPodcasts(): List<Podcast>? {
        val result = httpClient.get("$restUrl/list")
        return if (result.status.isSuccess()) {
            val body = result.bodyAsText()
            val entity = json.decodeFromString<PodcastsEntity>(body)
            entity.podcasts.map { Podcast(it, this) }
        } else null
    }

    suspend fun createConnection(ipAddress: String): PodcastConnection? {
        val connection = establishPodcastConnection("/ws", ipAddress, true) ?: return null
        connections[connection.podcastId] = connection
        return connection
    }

    suspend fun getConnection(ipAddress: String, id: UInt): PodcastConnection? {
        return connections[id]
            ?: establishPodcastConnection("/listen?id=$id", ipAddress, false)
                ?.also { connections[id] = it }
    }

    private suspend fun establishPodcastConnection(urlPath: String, ipAddress: String, host: Boolean): PodcastConnection? {
        val websocket = httpClient.webSocketSession(websocketUrl + urlPath)
        val gateway = Gateway(websocket, this)

        val helloEvent = gateway.awaitEvent<HelloEvent>(EventType.HELLO) ?: return null

        val selector = SelectorManager(Dispatchers.IO)
        val socket = aSocket(selector).udp().connect(helloEvent.address)
        val audioSocket = AudioSocket(host, socket, helloEvent.secret)

        val localPort = socket.localAddress.toJavaAddress().port
        val connectedEvent = ConnectedEvent(ipAddress, localPort)
        gateway.send(connectedEvent.prepare(this))

        return PodcastConnection(helloEvent.podcastId, gateway, audioSocket, this)
    }

    suspend fun closeConnection(podcastId: UInt) {
        connections.remove(podcastId)?.close()
    }

}

data class Credentials(
    var clientId: Long,
    var clientSecret: String
)

fun podcasts(credentialsBuilder: Credentials.() -> Unit): DiscordPodcasts {
    val credentials = Credentials(-1, "")
    credentialsBuilder.invoke(credentials)
    if (credentials.clientId < 0 || credentials.clientSecret.isEmpty()) {
        throw Exception("Invalid credentials")
    }
    return DiscordPodcasts(credentials.clientId, credentials.clientSecret)
}