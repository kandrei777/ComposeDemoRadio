package com.example.myradio.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myradio.data.repository.SyncStationsHelper
import com.example.myradio.domain.model.RadioStation
import com.example.myradio.domain.model.RadioStream
import com.example.myradio.domain.repository.PlayerControlRepository
import com.example.myradio.domain.repository.PlayerState
import com.example.myradio.domain.repository.RadioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val dbRepository: RadioRepository,
    private val syncHelper: SyncStationsHelper,
    private val player: PlayerControlRepository,
) : ViewModel(), MainActions {

    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _loading = MutableStateFlow(true)
    private val _view = MutableStateFlow(FilterMode.ALL)
    private val _currentPlayingStationId = MutableStateFlow<Int?>(null)

    // Single State production reactor
    val uiState: StateFlow<MainUiState> = combine(
        dbRepository.getAllStationsFlow(),    // 0
        dbRepository.getRecentStationsFlow(), // 1
        _currentPlayingStationId,             // 2
        player.state,                         // 3
        _errorMessage,                        // 4
        _loading,                             // 5
        _view                                 // 6
    ) { args ->
        // Безопасное приведение типов из массива аргументов комбайна
        val allStations = args[0] as List<RadioStation>
        val recentStations = args[1] as List<RadioStation>
        val playingId = args[2] as Int?
        val playerState = args[3] as PlayerState
        val error = args[4] as String?
        val loading = args[5] as Boolean
        val currentFilterMode = args[6] as FilterMode

        val displayStations = when (currentFilterMode) {
            FilterMode.ALL -> allStations
            FilterMode.FAVORITES -> allStations.filter { it.isFavorite }
            FilterMode.RECENT -> recentStations
        }.map { it.toStation() }

        var playingStation = if (playingId != null) {
            allStations.find { it.id == playingId }
        } else {
            null
        }

        if (playingStation == null && playerState.currentUri != null) {
            playingStation = allStations.find { station ->
                station.streams.any { stream -> stream.uri == playerState.currentUri }
            }
        }

        val uiPlayerState = playingStation?.toStation()?.let { station ->
            playerState.toUiPlayerState(
                errorMsg = error,
                playingStation = station
            )
        }

        MainUiState(
            stations = displayStations,
            isLoading = loading,
            playerState = uiPlayerState,
            filterMode = currentFilterMode
        ).also {
            Timber.d("UI State: ${it.debugOut()}")
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    init {
        startSyncStations()
    }

    private fun startSyncStations() {
        viewModelScope.launch {
            try {
                syncHelper.syncStationsIfNeeded()
            } catch (e: Exception) {
                Timber.e(e, "Synchronization failed")
                _errorMessage.value = e.localizedMessage
            } finally {
                _loading.value = false
            }
        }
    }

    override fun onPlayerAction() {
        val state = uiState.value.playerState ?: return
        viewModelScope.launch {
            when (state.playerStatus) {
                UiPlayerStatus.BUFFERING,
                UiPlayerStatus.PLAYING -> player.pause()

                UiPlayerStatus.PAUSED,
                UiPlayerStatus.ERROR -> player.resume()

                UiPlayerStatus.INITIALIZING -> Unit
            }
        }
    }

    override fun onPlayStation(station: Station) {
        viewModelScope.launch {
            var stream = station.streams.find { it.id == station.streamId }
            if (stream == null) {
                stream = station.streams[0]
                dbRepository.updateLastStream(station.id, stream.id)
            }
            _currentPlayingStationId.value = station.id
            dbRepository.addToRecent(station.id)
            player.play(stream.url, station.relativeVolume, artworkUrl = station.icon, title = station.title)
        }
    }

    override fun onChangeStream(station: Station, newStreamId: Int) {
        if (station.streamId != newStreamId) {
            val stream = station.streams.find { it.id == newStreamId }
            if (stream != null) {
                viewModelScope.launch {
                    dbRepository.updateLastStream(station.id, stream.id)
                    _currentPlayingStationId.value = station.id
                    player.play(stream.url, station.relativeVolume, station.icon, station.title)
                }
            } else {
                Timber.e("Stream id $newStreamId not found in $station")
            }
        }
    }

    override fun onToggleFavorite(station: Station) {
        viewModelScope.launch {
            dbRepository.toggleFavorite(station.id, !station.isFavorite)
        }
    }

    override fun onSwitchAll() {
        _view.value = FilterMode.ALL
    }

    override fun onSwitchFavorites() {
        _view.value = FilterMode.FAVORITES
    }

    override fun onSwitchRecents() {
        _view.value = FilterMode.RECENT
    }

    override fun onVolumeChanging(station: Station, volume: Float) {
        viewModelScope.launch {
            player.setRelativeVolume(volume)
        }
    }

    override fun onVolumeChanged(station: Station, volume: Float) {
        viewModelScope.launch {
            player.setRelativeVolume(volume)
            dbRepository.updateStationVolume(station.id, volume)
        }
    }
}

private fun PlayerState.toUiPlayerState(
    errorMsg: String?,
    playingStation: Station
): UiPlayerState {
    val playerStatus = when (status) {
        PlayerState.PlayStatus.INIT -> UiPlayerStatus.INITIALIZING
        PlayerState.PlayStatus.READY -> UiPlayerStatus.PAUSED
        PlayerState.PlayStatus.BUFFERING -> UiPlayerStatus.BUFFERING
        PlayerState.PlayStatus.ERROR -> UiPlayerStatus.ERROR
        PlayerState.PlayStatus.PLAYING -> UiPlayerStatus.PLAYING
    }
    return UiPlayerState(
        station = playingStation,
        networkTitle = trackInfo?.title,
        streamId = playingStation.streamId,
        playerStatus = playerStatus,
        errorMessage = errorMsg
    )
}

fun RadioStation.toStation(): Station {
    return Station(
        id = this.id,
        title = this.title,
        description = this.description ?: "",
        icon = this.iconUri ?: "",
        url = this.websiteUrl ?: "",
        isFavorite = this.isFavorite,
        streams = this.streams.map { it.toStream() },
        streamId = (streams.find { it.id == lastSelectedStreamId } ?: streams[0]).id,
        relativeVolume = relativeVolume,
        genres = genres.map { it.title }
    )
}

fun RadioStream.toStream(): Stream {
    return Stream(
        id = this.id,
        url = this.uri,
        quality = this.mediaType
    )
}
