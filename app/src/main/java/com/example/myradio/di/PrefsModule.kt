package com.example.myradio.di

import com.example.myradio.data.preferences.PrefsHelperImpl
import com.example.myradio.domain.preferences.PrefsHelper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface PrefsModule {
    @Singleton
    @Binds
    fun bindPrefsHelper(
        impl: PrefsHelperImpl
    ): PrefsHelper
}