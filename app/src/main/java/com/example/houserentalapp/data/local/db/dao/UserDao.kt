package com.example.houserentalapp.data.local.db.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.core.database.getStringOrNull
import com.example.houserentalapp.data.local.db.DatabaseHelper
import com.example.houserentalapp.data.local.db.entity.UserPreferenceEntity
import com.example.houserentalapp.data.local.db.tables.UserPreferenceTable
import com.example.houserentalapp.data.local.db.tables.UserTable
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.model.enums.UserField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// User-related tables
class UserDao(private val dbHelper: DatabaseHelper) {
    val writableDB: SQLiteDatabase
        get() = dbHelper.writableDatabase

    val readableDB: SQLiteDatabase
        get() = dbHelper.readableDatabase

    // -------------- CREATE --------------
    suspend fun insertUser(newUser: User): Long = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(UserTable.COLUMN_NAME, newUser.name)
            put(UserTable.COLUMN_EMAIL, newUser.email)
            put(UserTable.COLUMN_PHONE, newUser.phone)
            put(UserTable.COLUMN_HASHED_PASSWORD, newUser.password)
            put(UserTable.COLUMN_CREATED_AT, newUser.createdAt)
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
    suspend fun getUserById(userId: Long): User = withContext(Dispatchers.IO) {
        val cursor = readableDB.query(
            UserTable.TABLE_NAME,
            null,
            "${UserTable.COLUMN_ID} = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        cursor.use {
             if (it.moveToFirst())
                return@withContext mapCursorToUser(it)
        }

        throw IllegalArgumentException("User Not found at the given id: $userId")
    }

    suspend fun getUserByPhone(phone: String): User? =
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
                    mapCursorToUser(it)
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

    fun updateUser(modifiedUser: User, updatedFields: List<UserField>) : Int {
        val values = ContentValues()
        updatedFields.forEach { field ->
            when (field) {
                UserField.NAME -> values.put(UserTable.COLUMN_NAME, modifiedUser.name)
                UserField.PHONE -> values.put(UserTable.COLUMN_PHONE, modifiedUser.phone)
                UserField.EMAIL -> values.put(UserTable.COLUMN_EMAIL, modifiedUser.email)
                UserField.PROFILE_IMAGE -> { }
            }
        }

        return writableDB.update(
            UserTable.TABLE_NAME,
            values,
            "${UserTable.COLUMN_ID} = ?",
            arrayOf(modifiedUser.id.toString())
        )
    }

    // -------------- HELPER METHODS --------------
    private fun mapCursorToUser(cursor: Cursor): User {
        with(cursor) {
            return User(
                id = getLong(cursor.getColumnIndexOrThrow(UserTable.COLUMN_ID)),
                name = getString(getColumnIndexOrThrow(UserTable.COLUMN_NAME)),
                email = getStringOrNull(getColumnIndexOrThrow(UserTable.COLUMN_EMAIL)),
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