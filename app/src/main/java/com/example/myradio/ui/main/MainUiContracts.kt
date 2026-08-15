package com.example.myradio.ui.main

import androidx.compose.runtime.Immutable

@Immutable
data class Station(
    val id: Int,
    val title: String,
    val description: String,
    val icon: String,
    val url: String,
    val isFavorite: Boolean,
    val streams: List<Stream>,
    val genres: List<String>,
    val streamId: Int,
    val relativeVolume: Float,
)

@Immutable
data class Stream(
    val id: Int,
    val url: String,
    val quality: String = "Unknown",
)

@Immutable
data class UiPlayerState(
    val station: Station,
    val networkTitle: String?,
    val streamId: Int,
    val playerStatus: UiPlayerStatus = UiPlayerStatus.INITIALIZING,
    val errorMessage: String? = null,
){
    /** Out only values we are tracking now */
    fun debugOut() = "Station: ${station.title}, status: $playerStatus, error: $errorMessage, netTitle: $networkTitle"
}

@Immutable
data class MainUiState(
    val stations: List<Station> = emptyList(),
    val favoriteStations: List<Station> = emptyList(),
    val isLoading: Boolean = true,
    val filterMode: FilterMode = FilterMode.ALL,
    val playerState: UiPlayerState? = null,
) {
    fun debugOut() = "isLoading:$isLoading, Filter:$filterMode, ${playerState?.debugOut()}"
}

enum class UiPlayerStatus {
    INITIALIZING,  // Ждем готовности
    BUFFERING,  // Буферизация, соединяемся
    PLAYING,
    PAUSED,
    ERROR,
}

enum class FilterMode {
    ALL, // Show all stations
    FAVORITES, // show favorite stations
    RECENT, // Show recent played stations
}

interface MainActions {
    fun onPlayerAction()
    fun onPlayStation(station: Station)
    fun onChangeStream(station: Station, newStreamId: Int)
    fun onToggleFavorite(station: Station)
    fun onSwitchAll()
    fun onSwitchFavorites()
    fun onSwitchRecents()
    fun onVolumeChanging(station: Station, volume: Float)
    fun onVolumeChanged(station: Station, volume: Float)
}
