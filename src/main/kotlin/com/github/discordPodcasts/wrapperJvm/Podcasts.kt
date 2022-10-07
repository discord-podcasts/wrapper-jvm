package com.github.discordPodcasts.wrapperJvm

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.serialization.json.Json
import java.util.concurrent.ForkJoinPool

object Podcasts : RestClient() {
    internal val coroutineScope = CoroutineScope(ForkJoinPool.commonPool().asCoroutineDispatcher())
    internal const val endpoint = "podcasts.myra.bot"
    internal val httpClient = HttpClient(CIO) {
        install(WebSockets)
        defaultRequest {
            val auth = authentication ?: throw Exception("Missing authentication data")
            header("clientId", auth.clientId)
            header("token", auth.token)
        }
    }
    internal val json = Json
    internal var authentication: Authentication? = null

    internal class Authentication(val clientId: String, val token: String)
}