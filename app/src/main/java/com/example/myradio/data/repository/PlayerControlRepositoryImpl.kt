package com.example.myradio.data.repository

import android.content.ComponentName
import android.content.Context
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.myradio.domain.repository.PlayerControlRepository
import com.example.myradio.domain.repository.PlayerState
import com.example.myradio.service.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject

class PlayerControlRepositoryImpl @Inject constructor(
    @param:ApplicationContext
    private val context: Context,
    private val scope: CoroutineScope,
    private val retryPolicy: RetryPolicy,
) : PlayerControlRepository {

    sealed interface UserRequest {
        data class PlayData(
            val uri: String,
            val artworkUrl: String? = null,
            val title: String? = null,
            val relativeVolume: Float = 1f,
        )

        data object STOP : UserRequest

        data class Pause(
            val data: PlayData, // Store play data to resume later
        ) : UserRequest

        data class Play(
            val data: PlayData,
            val timestamp: Long = 0
        ) : UserRequest {
            fun retry() = this.copy(timestamp = System.currentTimeMillis())
        }
    }

    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val _controller = MutableStateFlow<MediaController?>(null)
    private val _request = MutableStateFlow<UserRequest>(UserRequest.STOP)

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val playerListener = object : Player.Listener {

        override fun onEvents(player: Player, events: Player.Events) {
            if (events.containsAny(Player.EVENT_PLAYBACK_STATE_CHANGED, Player.EVENT_IS_PLAYING_CHANGED)) {
                _state.update { currentState ->
                    val newStatus = when (player.playbackState) {
                        Player.STATE_BUFFERING -> PlayerState.PlayStatus.BUFFERING
                        Player.STATE_READY -> {
                            if (player.isPlaying) PlayerState.PlayStatus.PLAYING
                            else PlayerState.PlayStatus.READY
                        }

                        Player.STATE_ENDED -> PlayerState.PlayStatus.READY
                        Player.STATE_IDLE -> {
                            if (player.playerError != null) PlayerState.PlayStatus.ERROR
                            else PlayerState.PlayStatus.READY
                        }

                        else -> currentState.status
                    }

                    val clearError = newStatus == PlayerState.PlayStatus.PLAYING ||
                            newStatus == PlayerState.PlayStatus.BUFFERING

                    if (newStatus == PlayerState.PlayStatus.PLAYING) {
                        retryPolicy.reportOk()
                    }

                    currentState.copy(
                        currentUri = _controller.value?.currentMediaItem?.mediaId,
                        status = newStatus,
                        error = if (clearError) null else currentState.error
                    )
                }
            }

            if (events.contains(Player.EVENT_PLAYER_ERROR)) {
                val playbackError = player.playerError
                retryPolicy.reportError()
                _state.update { currentState ->
                    val errorMessage = playbackError?.localizedMessage
                        ?: playbackError?.message
                        ?: "Unknown playback error"
                    Timber.e(playbackError, "ExoPlayer internal error occurred: $errorMessage")
                    currentState.copy(
                        status = PlayerState.PlayStatus.ERROR,
                        error = errorMessage
                    )
                }
            }
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            with(mediaMetadata) {
                Timber.i("station: $station, title: $title, artist: ")
            }
            _state.update { it.copy(trackInfo = mediaMetadata.toTrackInfo()) }
        }
    }

    init {
        observeCommands()
        start()
    }

    private fun observeCommands() {
        combine(_controller, _request, retryPolicy.attempts) { controller, request, attempt ->
            if (controller == null) return@combine // Ждем, пока контроллер подключится

            when (request) {
                is UserRequest.Play -> {
                    val data = request.data
                    val currentMediaId = controller.currentMediaItem?.mediaId
                    val inError = controller.playbackState == Player.STATE_IDLE && controller.playerError != null

                    Timber.i("[$attempt]: Request uri to play: ${data.uri}. Is in error: $inError")

                    if (currentMediaId != request.data.uri || inError) {
                        Timber.i("prepare: ${data.uri}")
                        val mediaMetadata = MediaMetadata.Builder()
                            .setStation(data.title)
                            .setArtist(data.title)
                            .setArtworkUri(data.artworkUrl?.toUri())
                            .build()
                        val mediaItem = MediaItem.Builder()
                            .setMediaId(data.uri)
                            .setUri(data.uri)
                            .setMediaMetadata(mediaMetadata)
                            .build()
                        controller.setMediaItem(mediaItem)
                        controller.prepare()
                    }

                    Timber.i("Play: ${data.uri}")
                    if (controller.volume != data.relativeVolume) {
                        controller.volume = data.relativeVolume
                    }

                    if (!controller.playWhenReady) {
                        controller.play()
                    }
                }

                is UserRequest.Pause -> {
                    controller.pause()
                }

                is UserRequest.STOP -> {
                    controller.stop()
                }
            }
        }
            .flowOn(Dispatchers.Main.immediate)
            .launchIn(scope) // Запускаем сбор данных в скоупе корутин
    }

    private fun start() {
        if (_controller.value != null) return
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture?.addListener({
            try {
                val readyController = controllerFuture?.get()
                _controller.tryEmit(readyController)
                if (readyController != null) {
                    readyController.addListener(playerListener)
                    _state.update { currentState ->
                        val initialStatus = when (readyController.playbackState) {
                            Player.STATE_BUFFERING -> PlayerState.PlayStatus.BUFFERING
                            Player.STATE_READY -> {
                                if (readyController.isPlaying) PlayerState.PlayStatus.PLAYING
                                else PlayerState.PlayStatus.READY
                            }

                            Player.STATE_ENDED -> PlayerState.PlayStatus.READY
                            Player.STATE_IDLE -> PlayerState.PlayStatus.READY
                            else -> PlayerState.PlayStatus.INIT
                        }

                        currentState.copy(
                            currentUri = readyController.currentMediaItem?.mediaId,
                            status = initialStatus,
                            trackInfo = readyController.mediaMetadata.toTrackInfo()
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Can't gain media controller")
            }
        }, MoreExecutors.directExecutor())
    }

    override suspend fun play(
        uri: String,
        relativeVolume: Float,
        artworkUrl: String?,
        title: String?
    ) {
        retryPolicy.cancel()
        _request.emit(UserRequest.Play(UserRequest.PlayData(uri, artworkUrl, title, relativeVolume)))
    }

    override suspend fun pause() {
        retryPolicy.cancel()
        _request.update {
            when (it) {
                is UserRequest.Play -> UserRequest.Pause(it.data)
                else -> it
            }
        }
    }

    override suspend fun resume() {
        retryPolicy.cancel()
        _request.update { req ->
            when (req) {
                is UserRequest.Pause -> UserRequest.Play(req.data)
                is UserRequest.Play -> req.retry()
                UserRequest.STOP -> req
            }
        }
    }

    override suspend fun setRelativeVolume(relativeVolume: Float) {
        _request.update { req ->
            when (req) {
                is UserRequest.Pause -> req.copy(data = req.data.copy(relativeVolume = relativeVolume))
                is UserRequest.Play -> req.copy(data = req.data.copy(relativeVolume = relativeVolume))
                UserRequest.STOP -> req
            }
        }
    }
}

private fun MediaMetadata.toTrackInfo(): PlayerState.TrackInfo = PlayerState.TrackInfo(title = title?.toString())
