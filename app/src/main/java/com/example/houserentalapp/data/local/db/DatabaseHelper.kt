package com.example.houserentalapp.data.local.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.houserentalapp.data.local.db.tables.PropertyImagesTable
import com.example.houserentalapp.data.local.db.tables.PropertyAmenitiesTable
import com.example.houserentalapp.data.local.db.tables.PropertyTable
import com.example.houserentalapp.data.local.db.tables.UserInterestedPropertyTable
import com.example.houserentalapp.data.local.db.tables.UserPreferenceTable
import com.example.houserentalapp.data.local.db.tables.UserPropertyActionTable
import com.example.houserentalapp.data.local.db.tables.UserTable

class DatabaseHelper private constructor(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_NAME = "my_app.db"
        private const val DATABASE_VERSION = 1
        private var INSTANCE: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DatabaseHelper(context).also { INSTANCE = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.let {
          // TABLE CREATION
            it.execSQL(UserTable.CREATE_TABLE)
            it.execSQL(UserPreferenceTable.CREATE_TABLE) //
            it.execSQL(PropertyTable.CREATE_TABLE)
            it.execSQL(UserPropertyActionTable.CREATE_TABLE)
            it.execSQL(UserInterestedPropertyTable.CREATE_TABLE)
            it.execSQL(PropertyAmenitiesTable.CREATE_TABLE)
            it.execSQL(PropertyImagesTable.CREATE_TABLE)

          // INDEX CREATION
//            it.execSQL(UserTable.CREATE_INDEXES) // Let's see the performance without indexes first
//            it.execSQL(PropertyTable.CREATE_INDEX)
//            it.execSQL(PropertyImagesTable.CREATE_INDEX)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) { }
}