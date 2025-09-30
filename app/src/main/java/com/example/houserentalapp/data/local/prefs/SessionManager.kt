package com.example.houserentalapp.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import com.example.houserentalapp.presentation.utils.extensions.logError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.content.edit

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    suspend fun createSession(userId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            sharedPreferences.edit {
                putLong(KEY_USER_ID, userId)
                putBoolean(KEY_IS_LOGGED_IN, true)
            }
            true
        } catch (e: Exception) {
            logError("Error creating session", e)
            false
        }
    }

    suspend fun clearSession(): Boolean = withContext(Dispatchers.IO) {
        try {
            sharedPreferences.edit {
                remove(KEY_USER_ID)
                .putBoolean(KEY_IS_LOGGED_IN, false)
            }
            true
        } catch (e: Exception) {
            logError("Error clearing session", e)
            false
        }
    }

    fun getLoggedInUserId(): Long {
        return sharedPreferences.getLong(KEY_USER_ID, -1)
    }

    companion object {
        private const val PREF_NAME = "user_session"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
}