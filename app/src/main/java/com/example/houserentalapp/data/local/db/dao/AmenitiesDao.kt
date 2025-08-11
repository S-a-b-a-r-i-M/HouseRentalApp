package com.example.houserentalapp.data.local.db.dao

import android.content.ContentValues
import android.util.Log
import com.example.houserentalapp.data.local.db.DatabaseHelper
import com.example.houserentalapp.data.local.db.tables.InternalAmenitiesTable
import com.example.houserentalapp.data.local.db.tables.SocialAmenitiesTable

// Internal amenities, Social amenities

class AmenitiesDao(private val dbHelper: DatabaseHelper) {

    fun createSocialAmenity() {
        val values = ContentValues().apply {
            put(SocialAmenitiesTable.COLUMN_NAME, "Swimming Pool")
        }
        try {
            val id = dbHelper.writableDatabase.insert(SocialAmenitiesTable.TABLE_NAME, null, values)
            Log.i(TAG, "Social amenity ID: $id")
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: "Error inserting social amenity")
        }
    }

    fun createInternalAmenity() {
        val values = ContentValues().apply {
            put(InternalAmenitiesTable.COLUMN_NAME, "Air Conditioner")
            put(InternalAmenitiesTable.COLUMN_HAS_COUNT, 1)
        }
        try {
            val id = dbHelper.writableDatabase.insert(InternalAmenitiesTable.TABLE_NAME, null, values)
            Log.i(TAG, "Internal amenity ID: $id")
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: "Error inserting internal amenity")
        }
    }


    companion object {
        private const val TAG = "AmenitiesDao"
    }
}