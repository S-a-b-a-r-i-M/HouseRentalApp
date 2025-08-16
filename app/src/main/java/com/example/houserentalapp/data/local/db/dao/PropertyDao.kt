package com.example.houserentalapp.data.local.db.dao

import android.content.ContentValues
import android.util.Log
import com.example.houserentalapp.data.local.db.DatabaseHelper
import com.example.houserentalapp.data.local.db.tables.PropertyImagesTable
import com.example.houserentalapp.data.local.db.tables.PropertyInternalAmenitiesTable
import com.example.houserentalapp.data.local.db.tables.PropertySocialAmenitiesTable
import com.example.houserentalapp.data.local.db.tables.PropertyTable

// Property main table + images + internal amenities + social amenities + etc..

class PropertyDao(private val dbHelper: DatabaseHelper) {

    companion object {
        private const val TAG = "PropertyDao"
    }
}