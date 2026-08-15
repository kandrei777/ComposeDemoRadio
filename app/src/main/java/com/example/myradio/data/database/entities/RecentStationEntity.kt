package com.example.myradio.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recent_stations",
    foreignKeys = [
        ForeignKey(
            entity = StationEntity::class,
            parentColumns = ["id"],
            childColumns = ["stationId"],
            onDelete = ForeignKey.CASCADE // Удаляем станцию - удаляем запись
        )
    ],
    indices = [Index(value = ["stationId"], unique = true)]
)
data class RecentStationEntity(
    @PrimaryKey val stationId: Int,
    val timestamp: Long // для сортировки по времени прослушивания.
)
