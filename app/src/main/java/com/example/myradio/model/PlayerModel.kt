package com.example.myradio.model

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import java.util.*

class PlayerModel(application: Application) : AndroidViewModel(application) {
    private var _state by mutableStateOf(PlayerState.Ready)
    private var player: Player
    private var currentPlayingStationPosition by mutableStateOf(-1)
    private var currentSelectStationPosition by mutableStateOf(-1)
    private var _lastUserEvent by mutableStateOf<UserNotification?>(null)

    var stations = mutableStateListOf<StationItem>()
        private set

    val playerAction: PlayerAction? get() = _state.fabAction
    val lastUserEvent: UserNotification? get() = _lastUserEvent

    init {
        Store.loadData(application).map { StationItem(it, UUID.randomUUID()) }
            .toCollection(stations)
        player = Player(this)
        application.enforceCallingOrSelfPermission ("android.permission.INTERNET", "No permission")
    }

    val currentPlayingStation: StationItem? get() = stations.getOrNull(currentPlayingStationPosition)
    val currentSelectStation: StationItem? get() = stations.getOrNull(currentSelectStationPosition)

    fun selectStation(stationItem: StationItem) {
        currentSelectStationPosition = if (currentSelectStation == stationItem) {
            -1
        } else {
            stations.indexOf(stationItem)
        }
    }

    fun playStream(stream: Stream) {
        val index = currentSelectStation?.station?.streams?.indexOf(stream)?:-1
        if (index < 0) {
            message("Cannot find stream")
        } else {
            currentPlayingStationPosition = currentSelectStationPosition
            message("Start ${currentPlayingStation?.station?.title}, stream $index")
            player.play(stream)
        }
    }

    fun message(message: String) {
        _lastUserEvent = UserNotification(message)
    }

    fun onFavAction(action: PlayerAction) {
        when(action) {
            PlayerAction.Play -> player.resume()
            PlayerAction.Cancel -> player.cancel()
            PlayerAction.Pause -> player.pause()
        }
    }

    fun setPlayerState(state: PlayerState) {
        _state = state
    }

    override fun onCleared() {
        player.onCleared()
    }
}