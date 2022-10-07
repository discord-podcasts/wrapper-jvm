package com.github.discordPodcasts.wrapperJvm

data class AuthBuilder(var clientId: String, var token: String)

fun podcasts(builder: AuthBuilder.() -> Unit) {
    val auth = AuthBuilder("", "").apply(builder)
    if (auth.clientId.isEmpty()) throw Exception("Missing client id")
    if (auth.token.isEmpty()) throw Exception("Missing token")
    Podcasts.authentication = Podcasts.Authentication(auth.clientId, auth.token)
}