package com.example.myradio.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.myradio.ui.theme.LocalThemeController

@Composable
fun MainScreen(
    uiState: MainUiState,
    actions: MainActions,
    modifier: Modifier = Modifier,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearchActive by rememberSaveable { mutableStateOf(false) }

    // Состояние отображения диалога тем
    var showThemeDialog by remember { mutableStateOf(false) }

    val displayedStations = remember(uiState.stations, searchQuery) {
        if (searchQuery.isEmpty()) {
            uiState.stations
        } else {
            uiState.stations.filter { station ->
                station.title.contains(searchQuery, ignoreCase = true) ||
                        station.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            MainAppBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onClearSearch = {
                    searchQuery = ""
                    isSearchActive = false
                },
                onSearchActivate = { isSearchActive = true },
                // Открываем диалог по нажатию на кнопку в AppBar
                onShowThemeDialog = { showThemeDialog = true },
                isSearchActive = isSearchActive,
            )
        },
        bottomBar = {
            BottomToolbar(
                filterMode = uiState.filterMode,
                onSwitchAll = {
                    actions.onSwitchAll()
                    searchQuery = ""
                },
                onSwitchFavorites = {
                    actions.onSwitchFavorites()
                    searchQuery = ""
                },
                onSwitchRecents = {
                    actions.onSwitchRecents()
                    searchQuery = ""
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                StationsListContent(
                    stations = displayedStations,
                    isLoading = uiState.isLoading,
                    searchQuery = searchQuery,
                    onPlayStation = actions::onPlayStation,
                    onToggleFavorite = actions::onToggleFavorite,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )

                AnimatedVisibility(
                    visible = uiState.playerState != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    uiState.playerState?.let { playerState ->
                        RadioPlayer(
                            playerState = playerState,
                            onPlayPauseClick = actions::onPlayerAction,
                            onChangeStream = { streamId ->
                                actions.onChangeStream(playerState.station, streamId)
                            },
                        )
                    }
                }
            }
        }
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            LocalThemeController.current,
            onDismissRequest = { showThemeDialog = false }
        )
    }
}
