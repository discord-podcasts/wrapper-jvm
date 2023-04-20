package com.github.discordPodcasts.wrapperJvm

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class RestClient(
    private val clientId: Long,
    private val clientSecret: String
) {
    val baseUrl = "https://podcasts.myra.bot"
    val httpClient = HttpClient(CIO) {
        defaultRequest {
            header("client_id", clientId)
            header("client_secret", clientSecret)
        }
    }
    val json = Json {
        ignoreUnknownKeys = true
    }

    suspend inline fun <reified T> execute(route: String, httpMethod: HttpMethod = HttpMethod.Get): T {
        val res = httpClient.request("$baseUrl$route") {
            method = httpMethod
        }
        val body = res.bodyAsText()
        return json.decodeFromString(body)
    }

}