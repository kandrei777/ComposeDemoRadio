package com.example.myradio.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.myradio.domain.preferences.PrefsHelper
import com.example.myradio.domain.preferences.ThemeMode
import com.example.myradio.domain.preferences.ThemeStyle
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PrefsHelperImpl @Inject constructor(
    @ApplicationContext context: Context
) : PrefsHelper {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    override fun getThemeMode(): ThemeMode {
        val modeName = sharedPreferences.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(modeName ?: ThemeMode.SYSTEM.name)
        } catch (_: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    override fun saveThemeMode(mode: ThemeMode) {
        sharedPreferences.edit {
            putString(KEY_THEME_MODE, mode.name)
        }
    }

    override fun getThemeStyle(): ThemeStyle {
        val styleName = sharedPreferences.getString(KEY_THEME_STYLE, ThemeStyle.DEFAULT.name)
        return try {
            ThemeStyle.valueOf(styleName ?: ThemeStyle.DEFAULT.name)
        } catch (_: IllegalArgumentException) {
            ThemeStyle.DEFAULT
        }
    }

    override fun saveThemeStyle(style: ThemeStyle) {
        sharedPreferences.edit {
            putString(KEY_THEME_STYLE, style.name)
        }
    }

    override fun getLastSyncedVer(): Long =
        sharedPreferences.getLong(PARAM_LAST_SYNCED_VER, -1L)


    override fun saveLastSyncedVer(ver: Long) {
        sharedPreferences.edit(commit = true) {
            putLong(PARAM_LAST_SYNCED_VER, ver)
        }
    }

    companion object {
        private const val PREFS_NAME = "radio_prefs"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_THEME_STYLE = "key_theme_style"
        private const val PARAM_LAST_SYNCED_VER = "last_synced_version_code"
    }
}
