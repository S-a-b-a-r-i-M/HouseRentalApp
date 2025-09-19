package com.example.houserentalapp.data.local.db.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.houserentalapp.data.local.db.DatabaseHelper
import com.example.houserentalapp.data.local.db.entity.UserActionEntity
import com.example.houserentalapp.data.local.db.tables.UserPropertyActionTable
import com.example.houserentalapp.domain.model.enums.UserActionEnum
import java.sql.SQLException

// User and Properties Relation
class UserPropertyDao(private val dbHelper: DatabaseHelper) {
    private val writableDb: SQLiteDatabase
        get() = dbHelper.writableDatabase

    private val readableDB: SQLiteDatabase
        get() = dbHelper.readableDatabase

    // -------------- CREATE --------------
    fun insertUserAction(userId: Long, propertyId: Long, action: UserActionEnum): Long {
        val values = ContentValues().apply {
            put(UserPropertyActionTable.COLUMN_TENANT_ID, userId)
            put(UserPropertyActionTable.COLUMN_PROPERTY_ID, propertyId)
            put(UserPropertyActionTable.COLUMN_ACTION, action.readable)
        }

        val id = writableDb.insert(UserPropertyActionTable.TABLE_NAME, null, values)
        if (id == -1L)
            throw SQLException("Failed to insert to UserPropertyActionTable")

        return id
    }

    // -------------- READ --------------
    fun getPropertiesShortlistedState(userId: Long, propertyIds: List<Long>): Map<Long, Boolean> {
        val propertyIdsPlaceHolder = propertyIds.joinToString(",") { "?" }
        val whereClause = """
            ${UserPropertyActionTable.COLUMN_TENANT_ID} = ? AND
            ${UserPropertyActionTable.COLUMN_ACTION} = ? AND
            ${UserPropertyActionTable.COLUMN_PROPERTY_ID} IN ($propertyIdsPlaceHolder)
        """.trimIndent()
        val whereArgs = arrayOf(
            userId.toString(),
            UserActionEnum.SHORTLISTED.readable
        ) + propertyIds.map { it.toString() }
        val orderBy = "${UserPropertyActionTable.COLUMN_CREATED_AT} DESC"

        readableDB.query(
            UserPropertyActionTable.TABLE_NAME,
            arrayOf(UserPropertyActionTable.COLUMN_PROPERTY_ID),
            whereClause,
            whereArgs,
            null, null,
            orderBy
        ).use { cursor ->
            val shortlistedIdsSet = mutableSetOf<Long>()
            while (cursor.moveToNext()) {
                shortlistedIdsSet.add(
                    cursor.getLong(0
//                        cursor.getColumnIndexOrThrow(
//                            UserPropertyActionTable.COLUMN_PROPERTY_ID
//                        )
                    )
                )
            }

            val resultMap = mutableMapOf<Long, Boolean>()
            propertyIds.forEach { resultMap.put(it, shortlistedIdsSet.contains(it)) }
            return resultMap
        }
    }

    fun getUserActions(userId: Long, propertyIds: List<Long>): List<UserActionEntity> {
        val idPlaceHolders = propertyIds.joinToString(",") { "?" }
        val whereClause = """
            ${UserPropertyActionTable.COLUMN_TENANT_ID} = ? AND
            ${UserPropertyActionTable.COLUMN_PROPERTY_ID} IN ($idPlaceHolders)
            """.trimIndent()

        val whereArgs = arrayOf(userId.toString()) + propertyIds.map { it.toString() }

        readableDB.query(
            UserPropertyActionTable.TABLE_NAME,
            null,
            whereClause,
            whereArgs,
            null, null, null,
        ).use { cursor ->
            val userActionEntityList = mutableListOf<UserActionEntity>()
            while (cursor.moveToNext())
                userActionEntityList.add(parseUserActionEntity(cursor))

            return userActionEntityList
        }
    }

    fun getUserActions(userId: Long, propertyId: Long): List<UserActionEntity> {
        val whereClause = """
            ${UserPropertyActionTable.COLUMN_TENANT_ID} = ? AND
            ${UserPropertyActionTable.COLUMN_PROPERTY_ID} == ?
            """.trimIndent()

        val whereArgs = arrayOf(userId.toString(), propertyId.toString())

        readableDB.query(
            UserPropertyActionTable.TABLE_NAME,
            null,
            whereClause,
            whereArgs,
            null, null, null,
        ).use { cursor ->
            val userActionEntityList = mutableListOf<UserActionEntity>()
            while (cursor.moveToNext())
                userActionEntityList.add(parseUserActionEntity(cursor))

            return userActionEntityList
        }
    }

    // -------------- UPDATE --------------


    // -------------- DELETE --------------
    fun removeFromShortlists(userId: Long, propertyId: Long, action: UserActionEnum): Boolean {
        val whereClause = """
            ${UserPropertyActionTable.COLUMN_TENANT_ID} = ? AND
            ${UserPropertyActionTable.COLUMN_PROPERTY_ID} = ? AND
            ${UserPropertyActionTable.COLUMN_ACTION} = ?
            """.trimIndent()
        val whereArgs = arrayOf(userId.toString(), propertyId.toString(), action.readable)

        return writableDb.delete(
            UserPropertyActionTable.TABLE_NAME, whereClause, whereArgs
        ) > 0
    }

    // Helpers
    fun parseUserActionEntity(cursor: Cursor) = UserActionEntity(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(
                UserPropertyActionTable.COLUMN_ID
            )),
            propertyId = cursor.getLong(cursor.getColumnIndexOrThrow(
                UserPropertyActionTable.COLUMN_PROPERTY_ID
            )),
            action = cursor.getString(cursor.getColumnIndexOrThrow(
                UserPropertyActionTable.COLUMN_ACTION
            )),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(
                UserPropertyActionTable.COLUMN_CREATED_AT
            ))
        )
}