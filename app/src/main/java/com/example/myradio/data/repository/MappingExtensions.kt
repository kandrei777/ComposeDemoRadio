package com.example.myradio.data.repository

import com.example.myradio.data.database.entities.GenreEntity
import com.example.myradio.data.database.entities.StationWithStreams
import com.example.myradio.data.database.entities.StreamEntity
import com.example.myradio.domain.model.RadioGenre
import com.example.myradio.domain.model.RadioStation
import com.example.myradio.domain.model.RadioStream

fun StreamEntity.toDomain(): RadioStream {
    return RadioStream(
        id = this.id,
        uri = this.uri,
        mediaType = this.mediaType,
        mime = this.mime
    )
}
fun GenreEntity.toDomain(): RadioGenre {
    return RadioGenre(
        id = this.id,
        title = this.title,
        iconUrl = this.iconUrl,
        url = this.url
    )
}

fun StationWithStreams.toDomain(): RadioStation {
    return RadioStation(
        id = this.station.id,
        title = this.station.title,
        iconUri = this.station.iconUri,
        websiteUrl = this.station.websiteUrl,
        description = this.station.description,
        lastSelectedStreamId = this.station.lastSelectedStreamId,
        isFavorite = this.station.isFavorite,
        streams = this.streams.map { it.toDomain() },
        genres = this.genres.map { it.toDomain() },
        relativeVolume = this.station.relativeVolume,
    )
}