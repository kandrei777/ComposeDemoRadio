package com.example.myradio.ui.theme

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.myradio.domain.preferences.PrefsHelper
import com.example.myradio.domain.preferences.ThemeMode
import com.example.myradio.domain.preferences.ThemeStyle

class ThemeController(
    val isDynamicSupported: Boolean,
    private val prefsHelper: PrefsHelper,
    ) {

    private val _currentMode = mutableStateOf(prefsHelper.getThemeMode())
    val currentMode: State<ThemeMode> = _currentMode

    private val _currentStyle = mutableStateOf(prefsHelper.getThemeStyle())
    val currentStyle: State<ThemeStyle> = _currentStyle

    fun setMode(mode: ThemeMode) {
        _currentMode.value = mode
        prefsHelper.saveThemeMode(mode)
    }

    fun setStyle(style: ThemeStyle) {
        _currentStyle.value = style
        prefsHelper.saveThemeStyle(style)
    }
}

val LocalThemeController = staticCompositionLocalOf<ThemeController> {
    error("ThemeController not provided. Check MyRadioTheme.")
}
