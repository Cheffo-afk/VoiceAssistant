package com.cheffoafk.voiceassistant.data

import android.content.Context
import android.content.SharedPreferences
import com.cheffoafk.voiceassistant.ui.theme.PaletteOption
import com.cheffoafk.voiceassistant.ui.theme.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val PREFS_NAME = "voice_assistant_prefs"
private const val KEY_THEME_DARK_MODE = "theme_dark_mode"
private const val KEY_THEME_PALETTE = "theme_palette"

class ThemePreferencesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _preferences = MutableStateFlow(readPreferences())
    val preferences: StateFlow<ThemePreferences> = _preferences.asStateFlow()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == KEY_THEME_DARK_MODE || key == KEY_THEME_PALETTE) {
            _preferences.value = readPreferences()
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_THEME_DARK_MODE, enabled).apply()
    }

    fun setPalette(option: PaletteOption) {
        prefs.edit().putString(KEY_THEME_PALETTE, option.key).apply()
    }

    private fun readPreferences(): ThemePreferences {
        return ThemePreferences(
            isDarkMode = prefs.getBoolean(KEY_THEME_DARK_MODE, false),
            palette = PaletteOption.fromKey(prefs.getString(KEY_THEME_PALETTE, PaletteOption.BASE.key))
        )
    }
}
