package com.example.myradio.domain.repository

import com.example.myradio.domain.model.RadioStation
import kotlinx.coroutines.flow.Flow

/**
 * //Todo migrate from data models to domain models.
 */
interface RadioRepository {
    suspend fun toggleFavorite(stationId: Int, isFavorite: Boolean)
    fun getAllStationsFlow(): Flow<List<RadioStation>>
    fun getRecentStationsFlow(): Flow<List<RadioStation>>
    suspend fun updateLastStream(stationId: Int, streamId: Int)
    suspend fun addToRecent(stationId: Int)
    suspend fun updateStationVolume(stationId: Int, relativeVolume: Float)
}