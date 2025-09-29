package com.example.houserentalapp.data.local.prefs

import android.content.Context
import com.example.houserentalapp.presentation.utils.extensions.logError
import androidx.core.content.edit

class ThemePreference(context: Context) {
    private val sharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveTheme(theme: String): Boolean {
        try {
            sharedPreferences.edit { putString(KEY_PREFERRED_THEME, theme) }
            return true
        } catch (e: Exception) {
            logError("Error saveTheme", e)
            return false
        }
    }

    fun readTheme() = sharedPreferences.getString(KEY_PREFERRED_THEME, null)

    companion object {
        const val PREF_NAME = "theme"
        const val KEY_PREFERRED_THEME = "pref_theme"
    }
}