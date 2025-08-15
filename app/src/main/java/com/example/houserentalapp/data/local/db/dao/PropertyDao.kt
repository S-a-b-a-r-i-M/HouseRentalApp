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

    fun createProperty() {
        val values = ContentValues().apply {
            put(PropertyTable.COLUMN_LANDLORD_ID, 1) // must exist in UserTable
            put(PropertyTable.COLUMN_STREET_NAME, "Test Street")
            put(PropertyTable.COLUMN_LOCALITY, "Locality1")
            put(PropertyTable.COLUMN_CITY, "TestCity")
            put(PropertyTable.COLUMN_NAME, "Test Property")
            put(PropertyTable.COLUMN_KIND, "Apartment")
            put(PropertyTable.COLUMN_TYPE, "INDEPENDENT_HOUSE")
            put(PropertyTable.COLUMN_BHK, "2BHK")
            put(PropertyTable.COLUMN_BUILT_UP_AREA, "1200")
            put(PropertyTable.COLUMN_LOOKING_TO, "Rent")
            put(PropertyTable.COLUMN_FURNISHING_TYPE, "SEMI_FURNISHED")
        }
        try {
            val id = dbHelper.writableDatabase.insert(PropertyTable.TABLE_NAME, null, values)
            Log.i(TAG, "Property ID: $id")
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: "Error inserting property")
        }
    }

    fun createPropertySocialAmenity() {
        val values = ContentValues().apply {
            put(PropertySocialAmenitiesTable.COLUMN_PROPERTY_ID, 1)
            put(PropertySocialAmenitiesTable.COLUMN_AMENITY_ID, 1)
        }
        try {
            val id = dbHelper.writableDatabase.insert(PropertySocialAmenitiesTable.TABLE_NAME, null, values)
            Log.i(TAG, "Property social amenity insert: $id")
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: "Error inserting property social amenity")
        }
    }

    fun createPropertyInternalAmenity() {
        val values = ContentValues().apply {
            put(PropertyInternalAmenitiesTable.COLUMN_PROPERTY_ID, 1)
            put(PropertyInternalAmenitiesTable.COLUMN_AMENITY_ID, 1)
            put(PropertyInternalAmenitiesTable.COLUMN_COUNT, 2)
        }
        try {
            val id = dbHelper.writableDatabase.insert(PropertyInternalAmenitiesTable.TABLE_NAME, null, values)
            Log.i(TAG, "Property internal amenity insert: $id")
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: "Error inserting property internal amenity")
        }
    }

    fun createPropertyImage() {
        val values = ContentValues().apply {
            put(PropertyImagesTable.COLUMN_PROPERTY_ID, 1)
            put(PropertyImagesTable.COLUMN_IMAGE_URL, "https://example.com/image1.jpg")
            put(PropertyImagesTable.COLUMN_IS_PRIMARY, 1)
        }
        try {
            val id = dbHelper.writableDatabase.insert(PropertyImagesTable.TABLE_NAME, null, values)
            Log.i(TAG, "Property image ID: $id")
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: "Error inserting property image")
        }
    }

    companion object {
        private const val TAG = "PropertyDao"
    }
}