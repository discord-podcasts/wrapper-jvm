package com.github.discordPodcasts.wrapperJvm.connection

import com.github.discordPodcasts.wrapperJvm.DiscordPodcasts
import com.github.discordPodcasts.wrapperJvm.connection.gateway.Gateway
import com.github.discordPodcasts.wrapperJvm.connection.udp.AudioSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

data class PodcastConnection(
    val podcastId: UInt,
    val gateway: Gateway,
    val audioSocket: AudioSocket,
    val api: DiscordPodcasts,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    suspend fun close(){
        gateway.close()
        audioSocket.close()
        coroutineScope.cancel()
    }
}