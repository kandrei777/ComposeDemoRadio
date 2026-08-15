package com.example.myradio.di

import com.example.myradio.data.repository.ExponentialBackoffRetryPolicy
import com.example.myradio.data.repository.PlayerControlRepositoryImpl
import com.example.myradio.data.repository.RetryPolicy
import com.example.myradio.domain.repository.PlayerControlRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerModule {

    @Binds
    @Singleton
    abstract fun bindPlayerControlRepository(
        impl: PlayerControlRepositoryImpl
    ): PlayerControlRepository

    @Binds
    @Singleton
    abstract fun bindRetryPolicy(
        impl: ExponentialBackoffRetryPolicy
    ): RetryPolicy

    companion object {

        @Provides
        @Singleton
        fun provideApplicationScope(): CoroutineScope {
            return CoroutineScope(SupervisorJob() + CoroutineName("MainAppAcope"))
        }
    }
}
