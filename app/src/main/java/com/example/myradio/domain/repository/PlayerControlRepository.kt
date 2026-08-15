package com.example.myradio.domain.repository

import kotlinx.coroutines.flow.StateFlow

data class PlayerState(
    val currentUri: String? = null,
    val trackInfo: TrackInfo? = null,
    val status: PlayStatus = PlayStatus.INIT,
    val error: String? = null
) {
    enum class PlayStatus {
        INIT, // Плеер еще не готов, тут просто ждать
        READY, // Плеер полностью готов к работе, но нет Uri на воспроизведение
        BUFFERING, // Есть Uri, но идет подготовка, нужно просто ждать.
        PLAYING, // Есть Uri, Штатное воспроизведение.
        ERROR, // Есть, Uri, но воспроизведение невозможно. Нужны действия.
    }

    data class TrackInfo(
        val title: String? = null
    )
}

interface PlayerControlRepository {
    val state: StateFlow<PlayerState>

    suspend fun play(
        uri: String,
        relativeVolume: Float,
        artworkUrl: String? = null,
        title: String? = null
    )

    suspend fun pause()
    suspend fun resume()
    suspend fun setRelativeVolume(relativeVolume: Float)
}