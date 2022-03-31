package com.example.myradio.model

import android.util.Log
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException


internal class Player(val playerModel: PlayerModel) :
    com.google.android.exoplayer2.Player.Listener {
    private val player: ExoPlayer

    companion object {
        val TAG = Player::class.java.simpleName
    }

    init {
        player = ExoPlayer.Builder(playerModel.getApplication()).build()
        player.addListener(this)
    }

    fun play(stream: Stream) {
        try {
            // Build the media item.
            val mediaItem: MediaItem = MediaItem.fromUri(stream.url)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
            Log.v(TAG, "Start $stream")
        } catch (e: Exception) {
            e.printStackTrace()
            playerModel.message("Cannot play the stream")
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        Log.v(TAG, "Playback state: $playbackState")
        playerModel.setPlayerState(
            when (playbackState) {
                ExoPlayer.STATE_IDLE -> PlayerState.Ready
                ExoPlayer.STATE_BUFFERING -> PlayerState.Buffering
                ExoPlayer.STATE_ENDED -> PlayerState.Stopped
                ExoPlayer.STATE_READY -> if (playWhenReady) PlayerState.Playing else PlayerState.Paused
                else -> PlayerState.Ready
            }
        )
    }

    override fun onPlayerError(error: PlaybackException) {
        error.printStackTrace()
        playerModel.message("Error: ${error.errorCodeName}")
    }

    private fun log(message: String) {
        playerModel.message(message)
        Log.v(TAG, message)
    }

    fun resume() {
        player.play()
    }

    fun cancel() {
        player.stop()
    }

    fun pause() {
        player.pause()
    }

    fun onCleared() {
        player.release()
    }
}