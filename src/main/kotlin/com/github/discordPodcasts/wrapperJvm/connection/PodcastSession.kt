package com.github.discordPodcasts.wrapperJvm.connection

/**
 * @property id Session id.
 * @property connection Reference to podcast connection.
 */
data class PodcastSession(
    val id: String,
    val connection: PodcastConnection,
    private val manager: PodcastSessionManager
) {
    val podcastId = connection.podcastId

    suspend fun provide(audio: ByteArray, nonce: ByteArray) {
        connection.audioSocket.provide(audio, nonce)
    }

    suspend fun close() {
        manager.closeSession(id, podcastId)
    }

}