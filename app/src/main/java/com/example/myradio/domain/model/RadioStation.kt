package com.example.myradio.domain.model

data class RadioStation(
    val id: Int,
    val title: String,
    val iconUri: String?,
    val description: String?,
    val lastSelectedStreamId: Int?,
    val isFavorite: Boolean,
    val streams: List<RadioStream>,
    val genres: List<RadioGenre>,
    val websiteUrl: String?,
    val relativeVolume: Float,
)