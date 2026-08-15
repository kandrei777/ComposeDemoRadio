package com.example.myradio.data.repository

import com.example.myradio.data.database.dao.StationDao
import com.example.myradio.data.database.entities.RecentStationEntity
import com.example.myradio.domain.model.RadioStation
import com.example.myradio.domain.repository.RadioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RadioRepositoryImpl @Inject constructor(
    private val stationDao: StationDao
) : RadioRepository {

    override fun getAllStationsFlow(): Flow<List<RadioStation>> =
        stationDao.getActiveStationsWithStreams()
            .map { list -> list.map { it.toDomain() } }

    override fun getRecentStationsFlow(): Flow<List<RadioStation>> =
        stationDao.getRecentStationsWithStreams()
            .map { list -> list.map { it.toDomain() } }

    override suspend fun toggleFavorite(stationId: Int, isFavorite: Boolean) {
        stationDao.updateFavoriteStatus(stationId, isFavorite)
    }

    override suspend fun updateLastStream(stationId: Int, streamId: Int) {
        stationDao.updateLastSelectedStreamId(stationId, streamId)
    }
    override suspend fun addToRecent(stationId: Int) {
        stationDao.insertRecentStation(
            RecentStationEntity(stationId = stationId, timestamp = System.currentTimeMillis())
        )
        // Подрезаем историю
        stationDao.trimRecentStations()
    }

    override suspend fun updateStationVolume(stationId: Int, relativeVolume: Float) =
        stationDao.updateStationVolume(stationId, relativeVolume)
}