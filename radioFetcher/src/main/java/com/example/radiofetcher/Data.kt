package com.example.radiofetcher


data class WebStation(
    val id: Int,
    val name: String,
    val signal: String,
    val title: String,
    val url: String,
    val usePopup: Boolean
)

data class WebStream(
    val id: Int,
    val isContainer: Boolean,
    val mediaType: String,
    val mime: String,
    val url: String
)

data class WebResult (
    val station: WebStation,
    val streams: List<WebStream>
)

data class WebData(
    val result: WebResult
)


data class Stream(val mediaType: String, val mime: String, val url: String)
data class Station(
    val id: Int,
    val title: String,
    val description: String,
    val icon: String,
    val url: String,
    val streams: List<Stream>
)
