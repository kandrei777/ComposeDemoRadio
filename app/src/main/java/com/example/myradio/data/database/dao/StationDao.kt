package com.example.myradio.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.myradio.data.database.entities.GenreEntity
import com.example.myradio.data.database.entities.RecentStationEntity
import com.example.myradio.data.database.entities.StationEntity
import com.example.myradio.data.database.entities.StationGenreCrossRef
import com.example.myradio.data.database.entities.StationWithStreams
import com.example.myradio.data.database.entities.StreamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StationDao {

    // Чтение всех активных (не ушедших) станций со стримами
    @Transaction
    @Query("SELECT * FROM stations WHERE isGone = 0")
    fun getActiveStationsWithStreams(): Flow<List<StationWithStreams>>

    @Transaction
    @Query("SELECT * FROM stations WHERE webKey = :webKey LIMIT 1")
    suspend fun getStationByWebKey(webKey: String): StationWithStreams?

    @Query("SELECT * FROM streams WHERE stationId = :stationId")
    suspend fun getStreamsForStation(stationId: Int): List<StreamEntity>

    // Удаляем конкретные стримы по их ID (которые больше не актуальны)
    @Query("DELETE FROM streams WHERE id IN (:streamIds)")
    suspend fun deleteStreamsByIds(streamIds: List<Int>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreams(streams: List<StreamEntity>)

    @Query("DELETE FROM streams WHERE stationId = :stationId")
    suspend fun deleteStreamsByStationId(stationId: Int)

    @Query("SELECT * FROM stations")
    suspend fun getAllStationsDirect(): List<StationEntity>

    @Delete
    suspend fun deleteStation(station: StationEntity)

    @Transaction
    @Query("SELECT * FROM stations WHERE isFavorite = 1")
    fun getFavoriteStationsWithStreams(): Flow<List<StationWithStreams>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStation(station: StationEntity): Long

    @Update
    suspend fun updateStation(station: StationEntity)

    @Query("UPDATE stations SET relativeVolume = :relativeVolume WHERE id = :stationId")
    suspend fun updateStationVolume(stationId: Int, relativeVolume: Float)

    @Query("DELETE FROM stations")
    suspend fun clearAll()

    @Query("UPDATE stations SET isFavorite = :isFavorite WHERE id = :stationId")
    suspend fun updateFavoriteStatus(stationId: Int, isFavorite: Boolean)

    @Query("UPDATE stations SET lastSelectedStreamId = :lastSelectedStreamId WHERE id = :stationId")
    suspend fun updateLastSelectedStreamId(stationId: Int, lastSelectedStreamId: Int)

    /************** Genres *************/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenres(genres: List<GenreEntity>)

    @Query("SELECT * FROM genres")
    suspend fun getAllGenresDirect(): List<GenreEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenreCrossRefs(refs: List<StationGenreCrossRef>)

    @Delete
    suspend fun deleteGenreCrossRefs(refs: List<StationGenreCrossRef>)

    @Query(
        """
    DELETE FROM genres 
    WHERE id NOT IN (SELECT DISTINCT genreId FROM station_genre_cross_ref)
"""
    )
    suspend fun deleteOrphanedGenres()

    /************** Recent stations *************/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentStation(recent: RecentStationEntity)

    @Transaction
    @Query(
        """
        SELECT s.* FROM stations s 
        INNER JOIN recent_stations r ON s.id = r.stationId 
        WHERE s.isGone = 0
        ORDER BY r.timestamp DESC
    """
    )
    fun getRecentStationsWithStreams(): Flow<List<StationWithStreams>>

    @Query(
        """
        DELETE FROM recent_stations 
        WHERE stationId NOT IN (
            SELECT stationId FROM recent_stations 
            ORDER BY timestamp DESC 
            LIMIT 20
        )
    """
    )
    suspend fun trimRecentStations()
}