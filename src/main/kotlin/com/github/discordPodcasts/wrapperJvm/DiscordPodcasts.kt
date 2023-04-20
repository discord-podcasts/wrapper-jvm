package com.github.discordPodcasts.wrapperJvm

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*

@Suppress("unused")
class DiscordPodcasts(
    private val clientId: Long,
    private val clientSecret: String
) {
    val httpClient = HttpClient(CIO) {
        defaultRequest {
            header("client_id", clientId)
            header("client_secret", clientSecret)
        }
    }
}