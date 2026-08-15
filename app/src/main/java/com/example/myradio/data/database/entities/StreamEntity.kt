package com.example.myradio.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "streams",
    foreignKeys = [
        ForeignKey(
            entity = StationEntity::class,
            parentColumns = ["id"],
            childColumns = ["stationId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(value = ["stationId"]),
        Index(value = ["stationId", "uri"], unique = true)
    ]
)
data class StreamEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val stationId: Int,
    val uri: String,
    val mediaType: String,
    val mime: String,
)