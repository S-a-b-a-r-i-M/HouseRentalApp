package com.example.houserentalapp.data.local.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.houserentalapp.data.local.db.tables.InternalAmenitiesTable
import com.example.houserentalapp.data.local.db.tables.PropertyImagesTable
import com.example.houserentalapp.data.local.db.tables.PropertyInternalAmenitiesTable
import com.example.houserentalapp.data.local.db.tables.PropertySocialAmenitiesTable
import com.example.houserentalapp.data.local.db.tables.PropertyTable
import com.example.houserentalapp.data.local.db.tables.SocialAmenitiesTable
import com.example.houserentalapp.data.local.db.tables.UserInterestedPropertyTable
import com.example.houserentalapp.data.local.db.tables.UserPreferenceTable
import com.example.houserentalapp.data.local.db.tables.UserShortlistedPropertyTable
import com.example.houserentalapp.data.local.db.tables.UserTable
import com.example.houserentalapp.data.local.db.tables.UserViewsTable

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
            it.execSQL(UserViewsTable.CREATE_TABLE)
            it.execSQL(UserShortlistedPropertyTable.CREATE_TABLE)
            it.execSQL(UserInterestedPropertyTable.CREATE_TABLE)
            it.execSQL(SocialAmenitiesTable.CREATE_TABLE)
            it.execSQL(PropertySocialAmenitiesTable.CREATE_TABLE)
            it.execSQL(InternalAmenitiesTable.CREATE_TABLE)
            it.execSQL(PropertyInternalAmenitiesTable.CREATE_TABLE)
            it.execSQL(PropertyImagesTable.CREATE_TABLE)

          // INDEX CREATION
            it.execSQL(UserTable.CREATE_INDEXES)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) { }
}