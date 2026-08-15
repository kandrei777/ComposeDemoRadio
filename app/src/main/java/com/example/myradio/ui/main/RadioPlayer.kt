package com.example.myradio.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun RadioPlayer(
    playerState: UiPlayerState,
    onPlayPauseClick: () -> Unit,
    onChangeStream: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Информация о станции
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Иконка станции
                AsyncImage(
                    model = playerState.station.icon,
                    contentDescription = playerState.station.title,
                    modifier = Modifier
                        .width(60.dp)
                        .sizeIn(maxHeight = 60.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(2.dp)                        ,
                    contentScale = ContentScale.Fit,
                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                    error = painterResource(id = android.R.drawable.ic_menu_gallery),
                )

                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = playerState.station.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    PlayerStatusIndicator(
                        status = playerState.playerStatus,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                IconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier.size(48.dp),
                ) {
                    when (playerState.playerStatus) {
                        UiPlayerStatus.INITIALIZING -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }

                        UiPlayerStatus.BUFFERING,
                        UiPlayerStatus.PLAYING -> {
                            Icon(
                                imageVector = Icons.Filled.Pause,
                                contentDescription = "Pause",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp),
                            )
                        }

                        UiPlayerStatus.PAUSED -> {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Play",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp),
                            )
                        }

                        UiPlayerStatus.ERROR -> {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Retry",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(28.dp),
                            )
                        }

                    }
                }
            }

            // Ошибка (если есть)
            if (playerState.errorMessage != null && playerState.playerStatus == UiPlayerStatus.ERROR) {
                ErrorMessage(
                    message = playerState.errorMessage,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Выбор стрима (если есть несколько)
            if (playerState.station.streams.size > 1) {
                StreamSelector(
                    streams = playerState.station.streams,
                    currentStreamId = playerState.streamId,
                    onStreamSelect = onChangeStream,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun PlayerStatusIndicator(
    status: UiPlayerStatus,
    modifier: Modifier = Modifier,
) {
    val statusText = when (status) {
        UiPlayerStatus.INITIALIZING -> "Connecting..."
        UiPlayerStatus.PLAYING -> "Now playing"
        UiPlayerStatus.PAUSED -> "Paused"
        UiPlayerStatus.ERROR -> "Connection error"
        UiPlayerStatus.BUFFERING -> "Buffering"
    }

    val statusColor = when (status) {
        UiPlayerStatus.INITIALIZING -> MaterialTheme.colorScheme.primary
        UiPlayerStatus.BUFFERING -> MaterialTheme.colorScheme.secondary  // ← Переходное состояние
        UiPlayerStatus.PLAYING -> MaterialTheme.colorScheme.primary
        UiPlayerStatus.PAUSED -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        UiPlayerStatus.ERROR -> MaterialTheme.colorScheme.error
    }

    Text(
        text = statusText,
        style = MaterialTheme.typography.labelSmall,
        color = statusColor,
        modifier = modifier,
    )
}

@Composable
private fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(12.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

@Composable
private fun StreamSelector(
    streams: List<Stream>,
    currentStreamId: Int,
    onStreamSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Select stream",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            streams.forEachIndexed { index, stream ->
                StreamOption(
                    streamNumber = index + 1,
                    isSelected = stream.id == currentStreamId,
                    onClick = { onStreamSelect(stream.id) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun StreamOption(
    streamNumber: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface
                },
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 6.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Source $streamNumber",
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
    }
}

