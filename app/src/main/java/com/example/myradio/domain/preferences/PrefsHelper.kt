package com.example.myradio.domain.preferences

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class ThemeStyle {
    DEFAULT, HIGH_CONTRAST, MEDIUM_CONTRAST, DYNAMIC
}

interface PrefsHelper {
    fun getThemeMode(): ThemeMode
    fun saveThemeMode(mode: ThemeMode)

    fun getThemeStyle(): ThemeStyle
    fun saveThemeStyle(style: ThemeStyle)
    fun getLastSyncedVer(): Long
    fun saveLastSyncedVer(ver: Long)
}