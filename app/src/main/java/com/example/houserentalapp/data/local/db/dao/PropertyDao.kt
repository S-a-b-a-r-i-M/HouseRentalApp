package com.example.houserentalapp.data.local.db.dao

import android.database.sqlite.SQLiteDatabase
import com.example.houserentalapp.data.local.db.DatabaseHelper

// Property main table + images + internal amenities + social amenities + etc..
class PropertyDao(private val dbHelper: DatabaseHelper) {
    val writableDB: SQLiteDatabase
        get() = dbHelper.writableDatabase

    val readableDB: SQLiteDatabase
        get() = dbHelper.readableDatabase

    // -------------- CREATE --------------


}