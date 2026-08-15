package com.example.myradio.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myradio.data.database.dao.StationDao
import com.example.myradio.data.database.entities.GenreEntity
import com.example.myradio.data.database.entities.RecentStationEntity
import com.example.myradio.data.database.entities.StationEntity
import com.example.myradio.data.database.entities.StationGenreCrossRef
import com.example.myradio.data.database.entities.StreamEntity

@Database(
    entities = [StationEntity::class, StreamEntity::class, StationGenreCrossRef::class, GenreEntity::class, RecentStationEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2) // relativeVolume fill will be added automatically
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stationDao(): StationDao
}
