package com.example.houserentalapp.data.local.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.houserentalapp.data.local.db.tables.LeadPropertyMappingTable
import com.example.houserentalapp.data.local.db.tables.PropertyImagesTable
import com.example.houserentalapp.data.local.db.tables.PropertyAmenitiesTable
import com.example.houserentalapp.data.local.db.tables.PropertyTable
import com.example.houserentalapp.data.local.db.tables.SearchHistoryTable
import com.example.houserentalapp.data.local.db.tables.LeadTable
import com.example.houserentalapp.data.local.db.tables.UserPreferenceTable
import com.example.houserentalapp.data.local.db.tables.UserPropertyActionTable
import com.example.houserentalapp.data.local.db.tables.UserTable
import com.example.houserentalapp.domain.model.enums.LeadStatus
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo

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

    override fun onOpen(db: SQLiteDatabase?) {
        super.onOpen(db)
        /*
        Runs every time the database is opened (app start, reconnection)
        This is the critical one - without it, foreign keys won't work during normal app usage
        SQLite forgets this setting when the connection closes
         */
        db?.execSQL("PRAGMA foreign_keys = ON")
        logInfo("Foreign key constraints enabled")
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.let {
          // TABLE CREATION
            it.execSQL(UserTable.CREATE_TABLE)
            it.execSQL(UserPreferenceTable.CREATE_TABLE) //
            it.execSQL(PropertyTable.CREATE_TABLE)
            it.execSQL(UserPropertyActionTable.CREATE_TABLE)
            it.execSQL(LeadTable.CREATE_TABLE)
            it.execSQL(LeadPropertyMappingTable.CREATE_TABLE)
            it.execSQL(PropertyAmenitiesTable.CREATE_TABLE)
            it.execSQL(PropertyImagesTable.CREATE_TABLE)
            it.execSQL(SearchHistoryTable.CREATE_TABLE)

            // Insert dummy data
            try {
                insertInitialData(db)
                logInfo("<---------------- Dummy data are inserted successfully ---------------->")
            } catch (exp: Exception) {
                logError("error on loading dummy data: ${exp.message.toString()}")
            }


          // INDEX CREATION
//            it.execSQL(UserTable.CREATE_INDEXES) // Let's see the performance without indexes first
//            it.execSQL(PropertyTable.CREATE_INDEX)
//            it.execSQL(PropertyImagesTable.CREATE_INDEX)
        }
    }

    private fun upgradeFrom1To2(db: SQLiteDatabase){
      // Drop status column from LeadTable
        // 1. Rename Old Lead Table
        db.execSQL("ALTER TABLE ${LeadTable.TABLE_NAME} RENAME TO ${LeadTable.TABLE_NAME}_old;")
        // 2. Create New Lead Table (without status)
        db.execSQL(LeadTable.CREATE_TABLE)
        // 3. Copy data from old -> new(ignore status column)
        db.execSQL("""
                    INSERT INTO ${LeadTable.TABLE_NAME} (
                        ${LeadTable.COLUMN_ID},
                        ${LeadTable.COLUMN_TENANT_ID},
                        ${LeadTable.COLUMN_LANDLORD_ID},
                        ${LeadTable.COLUMN_NOTE},
                        ${LeadTable.COLUMN_CREATED_AT}
                    )
                    SELECT
                        ${LeadTable.COLUMN_ID},
                        ${LeadTable.COLUMN_TENANT_ID},
                        ${LeadTable.COLUMN_LANDLORD_ID},
                        ${LeadTable.COLUMN_NOTE},
                        ${LeadTable.COLUMN_CREATED_AT} 
                    FROM ${LeadTable.TABLE_NAME}_old;
                """.trimIndent())
        // 4. Drop Old Table
        db.execSQL("DROP TABLE ${LeadTable.TABLE_NAME}_old;")

      // Insert NewColumns In LeadPropertyMappingTable
        db.execSQL("""
            ALTER ${LeadPropertyMappingTable.TABLE_NAME} ADD COLUMN ${LeadPropertyMappingTable.COLUMN_STATUS} TEXT NOT NULL DEFAULT '${LeadStatus.NEW}';
            ALTER ${LeadPropertyMappingTable.TABLE_NAME} ADD COLUMN ${LeadPropertyMappingTable.COLUMN_NOTE} TEXT;
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (db != null)
            upgradeFrom1To2(db)

    }
}