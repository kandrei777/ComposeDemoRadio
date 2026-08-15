package com.example.myradio.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "genres",
    indices = [Index(value = ["title"], unique = true)]
)
data class GenreEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val iconUrl: String?,
    val url: String?
)
