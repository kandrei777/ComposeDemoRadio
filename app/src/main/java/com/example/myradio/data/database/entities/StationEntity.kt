package com.example.myradio.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stations",
    foreignKeys = [
        ForeignKey(
            entity = StreamEntity::class,
            parentColumns = ["id"],
            childColumns = ["lastSelectedStreamId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["lastSelectedStreamId"]),
        Index(value = ["webKey"], unique = true)
    ]
)

data class StationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Внутренний ID приложения
    val webKey: String, // Внешний ID, Генерируется фетчером.
    val title: String,
    val websiteUrl: String?,
    val iconUri: String?,
    val description: String?,
    val lastSelectedStreamId: Int?,
    val isFavorite: Boolean,
    val isGone: Boolean,
    @ColumnInfo(defaultValue = "1.0") val relativeVolume: Float = 1f,
)
