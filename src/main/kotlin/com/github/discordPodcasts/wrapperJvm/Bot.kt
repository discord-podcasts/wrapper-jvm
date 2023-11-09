package com.github.discordPodcasts.wrapperJvm


import bot.myra.diskord.common.Diskord
import bot.myra.diskord.common.cache.caches.default.DefaultGuildCache
import bot.myra.diskord.common.cache.caches.default.DefaultVoiceStateCache
import bot.myra.diskord.common.cache.caches.timeout.TimeoutChannelCache
import bot.myra.diskord.common.cache.caches.timeout.TimeoutMemberCache
import bot.myra.diskord.common.cache.caches.timeout.TimeoutMessageCache
import bot.myra.diskord.common.cache.caches.timeout.TimeoutUserCache
import bot.myra.diskord.common.entities.channel.VoiceChannel
import bot.myra.diskord.gateway.GatewayIntent
import bot.myra.diskord.gateway.connectGateway
import bot.myra.diskord.gateway.events.EventListener
import bot.myra.diskord.gateway.events.ListenTo
import bot.myra.diskord.gateway.events.impl.interactions.slashCommands.GuildSlashCommandEvent
import bot.myra.diskord.voice.VoiceConnection
import com.github.discordPodcasts.wrapperJvm.connection.PodcastSessionManager
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import kotlin.time.Duration.Companion.seconds

val scope = CoroutineScope(Dispatchers.Default)
val ip = runBlocking { HttpClient(CIO).get("https://api.ipify.org").bodyAsText() }

val podcasts = podcasts {
    clientId = -1
    clientSecret = "PODCASTS_CLIENT_ID"
}
val podcastsManager = PodcastSessionManager(podcasts)

suspend fun main() {
    Diskord("DISCORD_TOKEN_HERE").apply {
        addListeners(Listener)
        intents(GatewayIntent.GUILDS, GatewayIntent.GUILD_VOICE_STATES)
        cachePolicy {
            user = TimeoutUserCache(10.seconds).policy()
            guild = DefaultGuildCache().policy()
            member = TimeoutMemberCache(10.seconds).policy()
            voiceState = DefaultVoiceStateCache().policy()
            channel = TimeoutChannelCache(15.seconds).policy()
            message = TimeoutMessageCache(10.seconds).policy()
        }
    }.connectGateway()
    while (true) {
        delay(1000)
    }
}

val voiceConnections = mutableMapOf<String, VoiceConnection>()
suspend fun getVoiceConnection(channel: VoiceChannel): VoiceConnection {
    return voiceConnections.getOrPut(channel.id) {
        val voiceConnection = channel.requestConnection(mute = false, deaf = false)
        voiceConnection.openConnection()

        voiceConnection
    }
}

val channelToPodcast = mutableMapOf<String, UInt>()

object Listener : EventListener {

    @ListenTo(GuildSlashCommandEvent::class)
    suspend fun onCommand(e: GuildSlashCommandEvent) {
        when (e.command.name) {
            "host"   -> hostPodcastCommand(e)
            "listen" -> listenPodcastCommand(e)
            "list"   -> listPodcastsCommand(e)
            "stop"   -> stopPodcastCommand(e)
        }
    }

    private suspend fun listPodcastsCommand(e: GuildSlashCommandEvent) {
        val podcasts = podcasts.listPodcasts() ?: return e.acknowledge { content = "Failed" }
        e.acknowledge {
            content = if (podcasts.isEmpty()) "*No active podcasts*"
            else "Host - podcast\n" + podcasts.map { it.entity }.joinToString("\n") {
                "* **${it.host}:** ${it.id}"
            }
        }
    }

    private suspend fun listenPodcastCommand(e: GuildSlashCommandEvent) {
        val channel = e.member.getVoiceState()?.getChannel()?.value ?: return e.acknowledge { content = "Join a voice channel first" }
        val podcastId = e.getOption<Long>("id")?.toUInt() ?: throw MissingFormatArgumentException("Missing id option")

        val voiceConnection = getVoiceConnection(channel)
        val podcastConnection = podcastsManager.createSession(podcastId, channel.id) ?: return e.acknowledge { content = "Failed" }
        channelToPodcast[channel.id] = podcastId

        scope.launch {
            podcastConnection.connection.audioSocket.receiver.collect {
                voiceConnection.udp!!.audioProvider.provide(it)
            }
        }

        e.acknowledge { content = "Listening to podcast $podcastId" }
    }

    private suspend fun hostPodcastCommand(e: GuildSlashCommandEvent) {
        val channel = e.member.getVoiceState()?.getChannel()?.value ?: return e.acknowledge { content = "Join vc" }
        val voiceConnection = getVoiceConnection(channel)

        val podcastConnection = podcastsManager.hostPodcastSession(channel.id) ?: return e.acknowledge { content = "Failed" }
        channelToPodcast[channel.id] = podcastConnection.podcastId
        scope.launch {
            voiceConnection.udp!!.audioReceiver.consumeAsFlow().collect {
                podcastConnection.provide(it.payload, it.nonce)
            }
        }

        e.acknowledge { content = "Created podcast" }
    }

    private suspend fun stopPodcastCommand(e: GuildSlashCommandEvent) {
        val channel = e.member.getVoiceState()?.getChannel()?.value ?: return e.acknowledge { content = "Join vc" }
        val voiceConnection = getVoiceConnection(channel)

        val session = podcastsManager.getBySessionId(channel.id) ?: return e.acknowledge { content = "No active podcast" }
        session.close()

        voiceConnection.disconnect()
        voiceConnection.leave()
        voiceConnections.remove(channel.id)

        e.acknowledge { content = "Closed connection" }
    }

}