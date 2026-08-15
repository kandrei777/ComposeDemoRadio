package com.example.myradio.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun StationsListContent(
    stations: List<Station>,
    isLoading: Boolean,
    searchQuery: String,
    onPlayStation: (Station) -> Unit,
    onToggleFavorite: (Station) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    AnimatedContent(
        targetState = when {
            isLoading -> ContentState.LOADING
            stations.isEmpty() && searchQuery.isNotEmpty() -> ContentState.NO_SEARCH_RESULTS
            stations.isEmpty() -> ContentState.EMPTY
            else -> ContentState.CONTENT
        },
        modifier = modifier,
        label = "stations_content_animation",
    ) { state ->
        when (state) {
            ContentState.LOADING -> {
                LoadingStationsPlaceholder(
                    contentPadding = contentPadding,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            ContentState.EMPTY -> {
                EmptyStationsPlaceholder(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = contentPadding,
                )
            }

            ContentState.NO_SEARCH_RESULTS -> {
                NoSearchResultsPlaceholder(
                    searchQuery = searchQuery,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = contentPadding,
                )
            }

            ContentState.CONTENT -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = contentPadding,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    items(
                        items = stations,
                        key = { station -> station.id },
                        contentType = { "station" },
                    ) { station ->
                        StationCard(
                            station = station,
                            onPlay = { onPlayStation(station) },
                            onToggleFavorite = { onToggleFavorite(station) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                        )
                    }
                    item {
                        // Bottom spacing для плеера
                        Box(modifier = Modifier.padding(bottom = 16.dp))
                    }
                }
            }
        }
    }
}

private enum class ContentState {
    LOADING,
    EMPTY,
    NO_SEARCH_RESULTS,
    CONTENT,
}

@Composable
private fun LoadingStationsPlaceholder(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(5) { index ->
            StationCardShimmer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            )
        }
    }
}

@Composable
private fun EmptyStationsPlaceholder(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            Text(
                text = "📻",
                style = MaterialTheme.typography.displayLarge,
            )
            Text(
                text = "No stations available",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Try again later or check your connection",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun NoSearchResultsPlaceholder(
    searchQuery: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            Text(
                text = "🔍",
                style = MaterialTheme.typography.displayLarge,
            )
            Text(
                text = "No results for \"$searchQuery\"",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Try a different search term",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}
