package com.example.myradio.data.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import com.example.myradio.data.database.dao.StationDao
import com.example.myradio.data.database.entities.StationEntity
import com.example.myradio.data.database.entities.StreamEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

// Используем Robolectric для симуляции Android-окружения на JVM компьютера
@RunWith(RobolectricTestRunner::class)
class StationDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: StationDao

    @Before
    fun createDb() {
        // Создаем базу в памяти, включаем поддержку внешних ключей
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries() // Для тестов допустимо в одном потоке
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    db.execSQL("PRAGMA foreign_keys=ON;") // В SQLite внешние ключи нужно включать явно
                }
            })
            .build()

        dao = db.stationDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndReadStationWithStreams() = runBlocking {
        // 1. Создаем и вставляем станцию
        val station = StationEntity(
            id = 1,
            webKey = "key",
            websiteUrl = null,
            title = "Test Radio",
            iconUri = null,
            description = "Rock music",
            lastSelectedStreamId = null,
            isFavorite = false,
            isGone = false
        )
        dao.insertStation(station)

        // 2. Создаем и вставляем стримы для этой станции
        val streams = listOf(
            StreamEntity(id = 1, stationId = 1, uri = "http://stream1.mp3", mediaType = "MP3", mime = "audio/mpeg"),
            StreamEntity(id = 2, stationId = 1, uri = "http://stream2.mp3", mediaType = "MP3", mime = "audio/mpeg")
        )
        dao.insertStreams(streams)

        // 3. Читаем данные через Flow (берем первое значение с помощью .first())
        val result = dao.getActiveStationsWithStreams().first()

        // 4. Проверяем корректность связей
        assertEquals(1, result.size)
        assertEquals("Test Radio", result[0].station.title)
        assertEquals(2, result[0].streams.size)
        assertEquals("http://stream1.mp3", result[0].streams[0].uri)
    }

    @Test
    fun testUniqueConstraintOnStreams() = runBlocking {
        dao.insertStation(StationEntity(
            id = 1, title = "R1", iconUri = null, description = null, lastSelectedStreamId = null, isFavorite = false, isGone = false,
            webKey = "webKey",
            websiteUrl = "websiteUrl"
        ))

        // Вставляем первый стрим
        dao.insertStreams(listOf(StreamEntity(id = 1, stationId = 1, uri = "http://same.mp3", mediaType = "MP3", mime = "audio/mpeg")))

        // Пробуем вставить дубликат URI для той же станции. Из-за OnConflictStrategy.REPLACE он должен перезаписать старый, а не упасть
        dao.insertStreams(listOf(StreamEntity(id = 2, stationId = 1, uri = "http://same.mp3", mediaType = "MP3", mime = "audio/mpeg")))

        val result = dao.getActiveStationsWithStreams().first()
        assertEquals(1, result[0].streams.size) // Стрим должен остаться один
    }
}
