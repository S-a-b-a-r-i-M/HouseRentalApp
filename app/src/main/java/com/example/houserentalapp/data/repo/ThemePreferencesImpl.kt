package com.example.houserentalapp.data.repo

import android.content.Context
import com.example.houserentalapp.data.local.prefs.ThemePreference
import com.example.houserentalapp.domain.repo.ThemePreferences
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.domain.utils.Result

/*
  WHY IO DISPATCHER ISN"T NEEDED ?
    SharedPreferences operations are already fast:

    getString(), getInt() etc. → Read from memory cache (instant)
    edit().putString().apply() → Writes asynchronously to disk in background thread (non-blocking)
    edit().putString().commit() → Writes synchronously (blocking, but rarely needed)
 */

class ThemePreferencesImpl(context: Context) : ThemePreferences {
    private val themePreference = ThemePreference(context)

    override fun saveTheme(theme: String) : Result<Unit> {
        return try {
            if (themePreference.saveTheme(theme))
                Result.Success(Unit)
            else
                Result.Error("Error while saving theme")
        } catch (e: Exception) {
            logError(e.message.toString())
            Result.Error(e.message.toString())
        }
    }

    override fun getTheme(): Result<String?> {
        return try {
            Result.Success(themePreference.readTheme())
        } catch (e: Exception) {
            logError(e.message.toString())
            Result.Error(e.message.toString())
        }
    }
}