package com.example.houserentalapp.data.local.db.dao

import android.content.ContentValues
import android.util.Log
import com.example.houserentalapp.data.local.db.DatabaseHelper
import com.example.houserentalapp.data.local.db.tables.UserInterestedPropertyTable
import com.example.houserentalapp.data.local.db.tables.UserShortlistedPropertyTable
import com.example.houserentalapp.data.local.db.tables.UserViewsTable

// User Related Properties

class UserPropertyDao(private val dbHelper: DatabaseHelper) {
    fun createUserView() {
        val values = ContentValues().apply {
            put(UserViewsTable.COLUMN_TENANT_ID, 1)
            put(UserViewsTable.COLUMN_PROPERTY_ID, 1)
        }
        try {
            val id = dbHelper.writableDatabase.insert(UserViewsTable.TABLE_NAME, null, values)
            Log.i(TAG, "User view insert result: $id")
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: "Error inserting user view")
        }
    }

    fun createUserShortlist() {
        val values = ContentValues().apply {
            put(UserShortlistedPropertyTable.COLUMN_TENANT_ID, 1)
            put(UserShortlistedPropertyTable.COLUMN_PROPERTY_ID, 1)
        }
        try {
            val id = dbHelper.writableDatabase.insert(UserShortlistedPropertyTable.TABLE_NAME, null, values)
            Log.i(TAG, "Shortlist insert result: $id")
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: "Error inserting shortlist")
        }
    }

    fun createUserInterest() {
        val values = ContentValues().apply {
            put(UserInterestedPropertyTable.COLUMN_TENANT_ID, 1)
            put(UserInterestedPropertyTable.COLUMN_PROPERTY_ID, 1)
            put(UserInterestedPropertyTable.COLUMN_STATUS, "Pending")
            put(UserInterestedPropertyTable.COLUMN_NOTE, "Looking forward to visiting")
        }
        try {
            val id = dbHelper.writableDatabase.insert(UserInterestedPropertyTable.TABLE_NAME, null, values)
            Log.i(TAG, "Interest insert result: $id")
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: "Error inserting interest")
        }
    }



    companion object {
        private const val TAG = "UserDao"
    }
}