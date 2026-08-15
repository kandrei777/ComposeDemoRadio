package com.example.myradio.di

import android.content.Context
import androidx.room.Room
import com.example.myradio.data.database.AppDatabase
import com.example.myradio.data.database.dao.StationDao
import com.example.myradio.data.repository.RadioRepositoryImpl
import com.example.myradio.domain.repository.RadioRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindRadioRepository(
        impl: RadioRepositoryImpl // Hilt сам создаст реализацию, если у неё есть @Inject constructor
    ): RadioRepository

    companion object {
        // Все @Provides методы переносим в companion object внутри абстрактного класса

        @Provides
        @Singleton
        fun provideAppDatabase(
            @ApplicationContext context: Context
        ): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "radio_database"
            ).build()
        }

        @Provides
        @Singleton
        fun provideRadioDao(database: AppDatabase): StationDao {
            return database.stationDao()
        }
    }
}
