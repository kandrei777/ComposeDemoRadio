package com.example.myradio.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.myradio.R

enum class PlayerAction(val imageVector: ImageVector, @StringRes val contentDescription: Int) {
    Pause(Icons.Filled.Pause,  R.string.ps_pause),
    Play(Icons.Filled.PlayArrow,  R.string.ps_play),
    Cancel(Icons.Filled.Autorenew,  R.string.ps_cancel)
}

enum class PlayerState(val fabAction: PlayerAction? = null) {
    Ready,
    Buffering(PlayerAction.Cancel),
    Playing(PlayerAction.Pause),
    Paused(PlayerAction.Play),
    Stopped
}