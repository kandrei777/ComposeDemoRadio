package com.example.myradio.ui.main

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

// Данные для preview
object PreviewData {
    val stations = listOf(
        Station(
            id = 1,
            title = "NPR Music",
            description = "National Public Radio - Music",
            icon = "https://via.placeholder.com/96",
            url = "https://example.com/npr",
            isFavorite = true,
            streamId = 1,
            streams = listOf(
                Stream(id = 1, url = "https://example.com/npr1", quality = "128kbps"),
                Stream(id = 2, url = "https://example.com/npr2", quality = "192kbps"),
            ),
            relativeVolume = 1f,
            genres = emptyList(),
        ),
        Station(
            id = 2,
            title = "BBC Radio 1",
            description = "British Broadcasting Corporation - Radio 1",
            icon = "https://via.placeholder.com/96",
            url = "https://example.com/bbc",
            isFavorite = false,
            streamId = 1,
            streams = listOf(
                Stream(id = 1, url = "https://example.com/bbc1", quality = "128kbps"),
            ),
            relativeVolume = 1f,
            genres = emptyList(),
        ),
        Station(
            id = 3,
            title = "Radio Paradise",
            description = "Independent Radio Paradise - Best Music",
            icon = "https://via.placeholder.com/96",
            url = "https://example.com/paradise",
            isFavorite = true,
            streamId = 1,
            streams = listOf(
                Stream(id = 1, url = "https://example.com/paradise1", quality = "256kbps"),
                Stream(id = 2, url = "https://example.com/paradise2", quality = "128kbps"),
                Stream(id = 3, url = "https://example.com/paradise3", quality = "192kbps"),
            ),
            relativeVolume = 1f,
            genres = emptyList(),
        ),
        Station(
            id = 4,
            title = "Jazz FM",
            description = "Best Jazz Music 24/7",
            icon = "https://via.placeholder.com/96",
            url = "https://example.com/jazzfm",
            isFavorite = false,
            streamId = 1,
            streams = listOf(
                Stream(id = 1, url = "https://example.com/jazzfm1", quality = "128kbps"),
            ),
            relativeVolume = 1f,
            genres = emptyList(),
        ),
    )

    val mockUiState = MainUiState(
        stations = stations,
        favoriteStations = stations.filter { it.isFavorite },
        isLoading = false,
        filterMode = FilterMode.ALL,
        playerState = null,
    )

    val mockUiStateWithPlayer = MainUiState(
        stations = stations,
        favoriteStations = stations.filter { it.isFavorite },
        isLoading = false,
        filterMode = FilterMode.ALL,
        playerState = UiPlayerState(
            station = stations[0],
            streamId = 0,
            playerStatus = UiPlayerStatus.PLAYING,
            networkTitle = "networkTitle"
        ),
    )

    val mockUiStateLoading = MainUiState(
        stations = emptyList(),
        favoriteStations = emptyList(),
        isLoading = true,
        filterMode = FilterMode.ALL,
        playerState = null,
    )

    val mockUiStateEmpty = MainUiState(
        stations = emptyList(),
        favoriteStations = emptyList(),
        isLoading = false,
        filterMode = FilterMode.ALL,
        playerState = null,
    )

    val mockUiStateError = MainUiState(
        stations = stations,
        favoriteStations = stations.filter { it.isFavorite },
        isLoading = false,
        filterMode = FilterMode.ALL,
        playerState = UiPlayerState(
            station = stations[1],
            streamId = 0,
            playerStatus = UiPlayerStatus.ERROR,
            errorMessage = "Failed to connect to stream. Check your connection.",
            networkTitle = "networkTitle",
        ),
    )

    object MockActions : MainActions {
        override fun onPlayerAction() {}
        override fun onPlayStation(station: Station) {}
        override fun onChangeStream(station: Station, newStreamId: Int) {}
        override fun onToggleFavorite(station: Station) {}
        override fun onSwitchAll() {}
        override fun onSwitchFavorites() {}
        override fun onSwitchRecents() {}
        override fun onVolumeChanging(station: Station, volume: Float) {}

        override fun onVolumeChanged(station: Station, volume: Float) {}
    }
}

// Previews
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
    Surface {
        MainScreen(
            uiState = PreviewData.mockUiState,
            actions = PreviewData.MockActions,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenWithPlayerPreview() {
    Surface {
        MainScreen(
            uiState = PreviewData.mockUiStateWithPlayer,
            actions = PreviewData.MockActions,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenLoadingPreview() {
    Surface {
        MainScreen(
            uiState = PreviewData.mockUiStateLoading,
            actions = PreviewData.MockActions,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenEmptyPreview() {
    Surface {
        MainScreen(
            uiState = PreviewData.mockUiStateEmpty,
            actions = PreviewData.MockActions,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenErrorPreview() {
    Surface {
        MainScreen(
            uiState = PreviewData.mockUiStateError,
            actions = PreviewData.MockActions,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StationCardPreview() {
    Surface {
        StationCard(
            station = PreviewData.stations[0],
            onPlay = {},
            onToggleFavorite = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RadioPlayerPreview() {
    Surface {
        RadioPlayer(
            playerState = PreviewData.mockUiStateWithPlayer.playerState!!,
            onPlayPauseClick = {},
            onChangeStream = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RadioPlayerErrorPreview() {
    Surface {
        RadioPlayer(
            playerState = PreviewData.mockUiStateError.playerState!!,
            onPlayPauseClick = {},
            onChangeStream = {},
        )
    }
}
