package com.github.discordPodcasts.wrapperJvm

import io.ktor.network.sockets.*

val ConnectedDatagramSocket.suitedAddress
    get() :SocketAddress {
        val testing = true
        return if (testing) this.localAddress
        else this.remoteAddress
    }