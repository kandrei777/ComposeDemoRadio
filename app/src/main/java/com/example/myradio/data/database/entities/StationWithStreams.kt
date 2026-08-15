package com.example.myradio.data.database.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class StationWithStreams(
    @Embedded val station: StationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "stationId"
    )
    val streams: List<StreamEntity>,

    @Relation(
        parentColumn = "id", // Станция
        entityColumn = "id", // Жанр
        associateBy = Junction( // Ассоциация между станцией и жанром
            value = StationGenreCrossRef::class, // через StationGenreCrossRef, где
            parentColumn = "stationId",      // ссылка на станцию
            entityColumn = "genreId"         // ссылка на жанр
        )
    )
    val genres: List<GenreEntity>
)