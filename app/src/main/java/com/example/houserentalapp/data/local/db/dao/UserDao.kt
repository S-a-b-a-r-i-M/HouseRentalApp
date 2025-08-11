package com.example.houserentalapp.data.local.db.dao

import android.content.ContentValues
import android.util.Log
import com.example.houserentalapp.data.local.db.DatabaseHelper
import com.example.houserentalapp.data.local.db.tables.UserPreferenceTable
import com.example.houserentalapp.data.local.db.tables.UserPreferenceTable.COLUMN_BHK
import com.example.houserentalapp.data.local.db.tables.UserTable
import com.example.houserentalapp.domain.utils.Result

// User-related tables

class UserDao(private val dbHelper: DatabaseHelper) {
    fun createUser() {
        val values = ContentValues().apply {
            put(UserTable.COLUMN_NAME, "Sample1")
            put(UserTable.COLUMN_EMAIL, "Sample1")
            put(UserTable.COLUMN_PHONE, "Sample1")
            put(UserTable.COLUMN_HASHED_PASSWORD, "Sample1")
        }

        try {
            val id = dbHelper.writableDatabase.insert(
                UserTable.TABLE_NAME, null, values
            )
            Log.i(TAG, "id of the user: $id")
        } catch (exp: Exception) {
            Log.e(TAG, "${exp.message}")
        }
    }

    fun createUserPreference() {
        val values = ContentValues().apply {
            put(UserPreferenceTable.COLUMN_USER_ID, 1)
            put(UserPreferenceTable.COLUMN_CITY, "Sample1")
            put(UserPreferenceTable.COLUMN_LOOKING_TO, "Sample1")
            put(UserPreferenceTable.COLUMN_BHK, "Sample1")
        }

        try {
            val id = dbHelper.writableDatabase.insert(
                UserPreferenceTable.TABLE_NAME, null, values
            )
            Log.i(TAG, "id of the user preference: $id")
        } catch (exp: Exception) {
            Log.e(TAG, "${exp.message}")
        }
    }

    companion object {
        private const val TAG = "UserDao"
    }
}