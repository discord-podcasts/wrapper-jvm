package com.github.discordPodcasts.wrapperJvm.models.events.read

import com.github.discordPodcasts.wrapperJvm.models.events.Event
import com.github.discordPodcasts.wrapperJvm.models.events.EventType
import io.ktor.network.sockets.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class HelloEvent(
    val host: Boolean,
    val ip: String,
    val port: Int,
    val secretKey: List<Byte>
) : Event(EventType.HELLO) {
    @Contextual
    val remoteAddress: InetSocketAddress = InetSocketAddress(ip, port)
}