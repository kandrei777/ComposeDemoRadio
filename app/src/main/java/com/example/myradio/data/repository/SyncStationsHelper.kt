package com.example.myradio.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.room.withTransaction
import com.example.myradio.data.database.AppDatabase
import com.example.myradio.data.database.dao.StationDao
import com.example.myradio.data.database.entities.GenreEntity
import com.example.myradio.data.database.entities.StationEntity
import com.example.myradio.data.database.entities.StationGenreCrossRef
import com.example.myradio.data.database.entities.StreamEntity
import com.example.myradio.data.model.RadioDataJson
import com.example.myradio.data.model.StationJson
import com.example.myradio.domain.preferences.PrefsHelper
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class SyncStationsHelper @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val stationDao: StationDao,
    private val appDatabase: AppDatabase,
    private val prefsHelper: PrefsHelper,
) {
    private val gson = Gson()

    suspend fun syncStationsIfNeeded() = withContext(Dispatchers.IO) {
        val currentVersionCode = getCurrentVersionCode(context)
        val lastSyncedVersion = prefsHelper.getLastSyncedVer()
        Timber.d("Sync: App ver:$currentVersionCode, Last ver: $lastSyncedVersion")
        if (currentVersionCode <= lastSyncedVersion) {
            return@withContext
        }
        Timber.d("Sync: Start json synchronisation")
        try {
            val jsonString = context.assets.open("stations_v2_ru.json").bufferedReader().use { it.readText() }
            val radioData = gson.fromJson(jsonString, RadioDataJson::class.java)
            val remoteStations: List<StationJson> = radioData.stations
            val genreEntities = radioData.genres.map {
                GenreEntity(
                    title = it.title,
                    iconUrl = it.iconUrl,
                    url = it.url
                )
            }
            val genreByTag = radioData.genres.associateBy { it.tag }

            appDatabase.withTransaction {
                // upsert genres
                stationDao.insertGenres(genreEntities)
                val incomingWebKeys = HashSet<String>(remoteStations.size)
                val allGenres = stationDao.getAllGenresDirect().associateBy { it.title }

                remoteStations.forEach { jsonStation ->
                    val uniqueWebKey = jsonStation.webKey
                    incomingWebKeys.add(uniqueWebKey)

                    val existingStation = stationDao.getStationByWebKey(uniqueWebKey)

                    if (existingStation == null) {
                        // Новая станция
                        val newStation = StationEntity(
                            webKey = uniqueWebKey,
                            title = jsonStation.title,
                            websiteUrl = jsonStation.websiteUrl,
                            iconUri = jsonStation.iconUrl,
                            description = jsonStation.description,
                            lastSelectedStreamId = null,
                            isFavorite = false,
                            isGone = false
                        )
                        val generatedId = stationDao.insertStation(newStation).toInt()
                        // Insert streams
                        val streams = jsonStation.streams.map { jsonStream ->
                            StreamEntity(
                                stationId = generatedId,
                                uri = jsonStream.streamUrl,
                                mediaType = jsonStream.mediaType,
                                mime = jsonStream.mime
                            )
                        }
                        stationDao.insertStreams(streams)

                        // Insert genres
                        val genreCrossRefs = jsonStation.genres.mapNotNull { jsonGenre ->
                            try {
                                val dbGenre = allGenres[genreByTag[jsonGenre]!!.title]!!
                                StationGenreCrossRef(stationId = generatedId, genreId = dbGenre.id)
                            } catch (_: Exception) {
                                Timber.e("Cannot find genre $jsonGenre for new station ${newStation.id}")
                                null
                            }
                        }
                        stationDao.insertGenreCrossRefs(genreCrossRefs)
                    } else { // Существующая станция
                        val currentStationId = existingStation.station.id
                        val dbStreams = existingStation.streams
                        val jsonStreamUris = jsonStation.streams.map { it.streamUrl }.toSet()

                        // Стримы на удаление: были в БД, но нет в JSON
                        val streamIdsToDelete = dbStreams
                            .filter { it.uri !in jsonStreamUris }
                            .map { it.id }

                        // Есть что удалять? - удаляем
                        if (streamIdsToDelete.isNotEmpty()) {
                            stationDao.deleteStreamsByIds(streamIdsToDelete)
                        }

                        // Ищем что добавить
                        val streamsToUpsert = jsonStation.streams.map { jsonStream ->
                            StreamEntity(
                                // Если стрим с таким URI уже был, сохраняем его ID для перезаписи метаданных
                                id = dbStreams.find { it.uri == jsonStream.streamUrl }?.id ?: 0,
                                stationId = currentStationId,
                                uri = jsonStream.streamUrl,
                                mediaType = jsonStream.mediaType,
                                mime = jsonStream.mime
                            )
                        }
                        if (streamsToUpsert.isNotEmpty()) {
                            stationDao.insertStreams(streamsToUpsert)
                        }

                        // слетел ли lastSelectedStreamId?
                        val currentSelectedStream = dbStreams.find { it.id == existingStation.station.lastSelectedStreamId }
                        if (currentSelectedStream != null && currentSelectedStream.id in streamIdsToDelete) {
                            // Если он был, но попал в удаленные, то подберем подходящий из существующих
                            // среди
                            val streamsNow = stationDao.getStreamsForStation(currentStationId)
                            val lastSelectedIdNow = (
                                    streamsNow.find { it.uri == currentSelectedStream.uri } ?: // Нашли?
                                    streamsNow.getOrNull(0) // нет, тогда первый из них
                                    )?.id
                            if (lastSelectedIdNow != null) {
                                stationDao.updateLastSelectedStreamId(currentStationId, lastSelectedIdNow)
                            }
                        }

                        // Обновляем жанры
                        val jsonGenres = jsonStation.genres.mapNotNull { genreByTag[it] }
                        val dbGenres = existingStation.genres
                        val genreCrossRefs = jsonGenres
                            .filterNot { g -> dbGenres.any { db -> db.title == g.title } }
                            .mapNotNull { genre ->
                                try {
                                    StationGenreCrossRef(stationId = currentStationId, genreId = allGenres[genre.title]!!.id)
                                } catch (_: Exception) {
                                    Timber.e("Cannot find genre $genre for existing station $currentStationId" )
                                    null
                                }
                            }
                        stationDao.insertGenreCrossRefs(genreCrossRefs)

                        val genresToDelete = dbGenres.filterNot { db -> jsonGenres.any { json -> json.title == db.title } }

                        if (genresToDelete.isNotEmpty()) {
                            val crossRefsToDelete = genresToDelete.map { genre ->
                                StationGenreCrossRef(stationId = currentStationId, genreId = genre.id)
                            }
                            // Вызываем точечное удаление записей кросс-рефа
                            stationDao.deleteGenreCrossRefs(crossRefsToDelete)
                        }
                    }
                }

                // Фаза очистки отсутствующих станций
                val allDbStations = stationDao.getAllStationsDirect()
                allDbStations.forEach { dbStation ->
                    if (dbStation.webKey !in incomingWebKeys) {
                        if (dbStation.isFavorite) {
                            stationDao.updateStation(dbStation.copy(isGone = true))
                        } else {
                            stationDao.deleteStation(dbStation)
                        }
                    }
                }

                // Удаление жанров не связанных со станциями
                stationDao.deleteOrphanedGenres()
            }

            prefsHelper.saveLastSyncedVer(currentVersionCode)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getCurrentVersionCode(context: Context): Long {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION") context.packageManager.getPackageInfo(context.packageName, 0)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode else @Suppress("DEPRECATION") packageInfo.versionCode.toLong()
        } catch (_: Exception) {
            0L
        }
    }
}
