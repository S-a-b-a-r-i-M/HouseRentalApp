package com.example.houserentalapp.data.local.db.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.houserentalapp.data.local.db.DatabaseHelper
import com.example.houserentalapp.data.local.db.entity.UserEntity
import com.example.houserentalapp.data.local.db.entity.UserPreferenceEntity
import com.example.houserentalapp.data.local.db.tables.UserPreferenceTable
import com.example.houserentalapp.data.local.db.tables.UserTable
import com.example.houserentalapp.presentation.utils.extensions.logError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// User-related tables
class UserDao(private val dbHelper: DatabaseHelper) {
    val writableDB: SQLiteDatabase
        get() = dbHelper.writableDatabase

    val readableDB: SQLiteDatabase
        get() = dbHelper.readableDatabase

    // -------------- CREATE --------------
    suspend fun insertUser(userEntity: UserEntity, hashedPassed: String): Long = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(UserTable.COLUMN_NAME, userEntity.name)
            put(UserTable.COLUMN_EMAIL, userEntity.email)
            put(UserTable.COLUMN_PHONE, userEntity.phone)
            put(UserTable.COLUMN_HASHED_PASSWORD, hashedPassed)
            put(UserTable.COLUMN_CREATED_AT, userEntity.createdAt)
        }

        writableDB.insertOrThrow(UserTable.TABLE_NAME, null, values)
    }

    suspend fun insertUserPreferences(entity: UserPreferenceEntity): Long =
        withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(UserPreferenceTable.COLUMN_USER_ID, entity.userId)
            put(UserPreferenceTable.COLUMN_CITY, entity.city)
            put(UserPreferenceTable.COLUMN_LOOKING_TO, entity.lookingTo)
            put(UserPreferenceTable.COLUMN_BHK, entity.bhk)
        }

        writableDB.insert(
            UserPreferenceTable.TABLE_NAME, null, values
        )
    }

    // -------------- READ --------------
    suspend fun getUserById(userId: Long): UserEntity? = withContext(Dispatchers.IO) {
        val cursor = readableDB.query(
            UserTable.TABLE_NAME,
            null,
            "${UserTable.COLUMN_ID} = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        cursor.use {
            return@withContext if (it.moveToFirst()) {
                mapCursorToUserEntity(it)
            } else null
        }
    }

    suspend fun getUserByPhone(phone: String): UserEntity? =
        withContext(Dispatchers.IO) {
            val db = dbHelper.readableDatabase
            val cursor = db.query(
                UserTable.TABLE_NAME,
                null,
                "${UserTable.COLUMN_PHONE} = ?",
                arrayOf(phone),
                null, null, null
            )

            cursor.use {
                return@withContext if (it.moveToFirst()) {
                    mapCursorToUserEntity(it)
                } else null
            }
        }

    suspend fun getUserPreferences(userId: Long): UserPreferenceEntity? =
        withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            UserPreferenceTable.TABLE_NAME,
            null,
            "${UserPreferenceTable.COLUMN_USER_ID} = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        cursor.use {
            return@withContext if (it.moveToFirst()) {
                mapCursorToUserPreferenceEntity(it)
            } else null
        }
    }

    suspend fun isPhoneNumberExists(phoneNumber: String): Boolean =
        withContext(Dispatchers.IO) {
        val cursor = readableDB.query(
            UserTable.TABLE_NAME,
            arrayOf(UserTable.COLUMN_ID),
            "${UserTable.COLUMN_PHONE} = ?",
            arrayOf(phoneNumber),
            null, null, null
        )

        cursor.use {
            return@withContext it.count > 0
        }
    }

    // -------------- UPDATE --------------
    suspend fun updateUserPreferences(entity: UserPreferenceEntity): Int = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(UserPreferenceTable.COLUMN_CITY, entity.city)
            put(UserPreferenceTable.COLUMN_LOOKING_TO, entity.lookingTo)
            put(UserPreferenceTable.COLUMN_BHK, entity.bhk)
        }

        writableDB.update(
            UserPreferenceTable.TABLE_NAME,
            values,
            "${UserPreferenceTable.COLUMN_USER_ID} = ?",
            arrayOf(entity.userId.toString())
        )
    }

    // -------------- HELPER METHODS --------------
    private fun mapCursorToUserEntity(cursor: Cursor): UserEntity {
        with(cursor) {
            return UserEntity(
                id = getLong(cursor.getColumnIndexOrThrow(UserTable.COLUMN_ID)),
                name = getString(getColumnIndexOrThrow(UserTable.COLUMN_NAME)),
                email = getString(getColumnIndexOrThrow(UserTable.COLUMN_EMAIL)),
                phone = getString(getColumnIndexOrThrow(UserTable.COLUMN_PHONE)),
                password = getString(getColumnIndexOrThrow(UserTable.COLUMN_HASHED_PASSWORD)),
                createdAt = getLong(getColumnIndexOrThrow(UserTable.COLUMN_CREATED_AT))
            )
        }
    }

    private fun mapCursorToUserPreferenceEntity(cursor: Cursor): UserPreferenceEntity {
        return UserPreferenceEntity(
            userId = cursor.getLong(cursor.getColumnIndexOrThrow(UserPreferenceTable.COLUMN_USER_ID)),
            city = cursor.getString(cursor.getColumnIndexOrThrow(UserPreferenceTable.COLUMN_CITY)),
            lookingTo = cursor.getString(cursor.getColumnIndexOrThrow(UserPreferenceTable.COLUMN_LOOKING_TO)),
            bhk = cursor.getString(cursor.getColumnIndexOrThrow(UserPreferenceTable.COLUMN_BHK))
        )
    }
}