package com.github.discordPodcasts.wrapperJvm.connection

import com.github.discordPodcasts.wrapperJvm.DiscordPodcasts
import com.github.discordPodcasts.wrapperJvm.ip

class PodcastSessionManager(private val api: DiscordPodcasts) {
    private val sessions = mutableMapOf<String, PodcastSession>()

    fun getBySessionId(sessionId: String): PodcastSession? {
        return sessions[sessionId]
    }

    suspend fun createSession(podcastId: UInt, sessionId: String): PodcastSession? {
        return sessions.getOrPut(sessionId) {
            val connection = api.getConnection(ip, podcastId) ?: return null
            PodcastSession(sessionId, connection, this)
        }
    }

    suspend fun closeSession(sessionId: String, podcastId: UInt) {
        sessions.remove(sessionId)
        // Podcast doesn't get used by any session
        if (sessions.values.none { it.podcastId == podcastId }) {
            api.closeConnection(podcastId)
        }
    }

    suspend fun hostPodcastSession(sessionId: String): PodcastSession? {
        val podcastConnection = api.createConnection(ip) ?: return null

        val session = PodcastSession(sessionId, podcastConnection, this)
        sessions[sessionId] = session
        return session
    }

}