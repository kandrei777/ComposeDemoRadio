package com.example.myradio.data.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    private val TEST_DB = "migration-test-db"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        listOf(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate1To2_containsCorrectDefaultValue() {
        var db = helper.createDatabase(TEST_DB, 1)
        db.execSQL(
            """
            INSERT INTO stations (webKey, title, websiteUrl, iconUri, description, lastSelectedStreamId, isFavorite, isGone) 
            VALUES ('rock_fm', 'Rock FM', 'https://rock.fm', null, 'Rock music station', null, 0, 0)
            """.trimIndent()
        )
        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true)

        val cursor = db.query("SELECT * FROM stations WHERE webKey = 'rock_fm'")
        try {
            if (cursor.moveToFirst()) {
                val relativeVolumeIndex = cursor.getColumnIndexOrThrow("relativeVolume")
                val volumeValue = cursor.getFloat(relativeVolumeIndex)
                assertEquals(1.0f, volumeValue, 0.001f)
            } else {
                throw AssertionError("Запись не найдена в базе данных после миграции")
            }
        } finally {
            cursor.close()
        }
    }
}
