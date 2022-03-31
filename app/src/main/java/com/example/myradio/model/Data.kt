package com.example.myradio.model

import java.util.*


data class Stream(val mime: String, val url: String)

data class Station(
    val title: String,
    val description: String,
    val icon: String,
    val url: String,
    val streams: List<Stream>
)

data class StationItem(
    val station: Station,
    val id: UUID = UUID.randomUUID()
)

data class UserNotification(
    val message: String,
    val id: UUID = UUID.randomUUID() // allow showing the same message
)