package com.example.houserentalapp.data.local.db.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.houserentalapp.data.local.db.DatabaseHelper
import com.example.houserentalapp.data.local.db.entity.PropertySummaryEntity
import com.example.houserentalapp.data.local.db.entity.UserActionEntity
import com.example.houserentalapp.data.local.db.tables.PropertyImagesTable
import com.example.houserentalapp.data.local.db.tables.PropertyTable
import com.example.houserentalapp.data.local.db.tables.UserPropertyActionTable
import com.example.houserentalapp.data.mapper.PropertyImageMapper
import com.example.houserentalapp.data.mapper.PropertyMapper
import com.example.houserentalapp.domain.model.Pagination
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

    fun getPropertySummariesByUserAction(
        userId: Long,
        pagination: Pagination,
        action: UserActionEnum
    ): List<PropertySummaryEntity> {
        val query = """
            SELECT 
                p.*,
                CASE
                    WHEN COUNT(p.${PropertyTable.COLUMN_ID}) > 0 THEN
                        '[' || GROUP_CONCAT(
                            json_object(
                                '${PropertyImagesTable.COLUMN_ID}', pi.${PropertyImagesTable.COLUMN_ID},
                                '${PropertyImagesTable.COLUMN_IMAGE_ADDRESS}', pi.${PropertyImagesTable.COLUMN_IMAGE_ADDRESS},
                                '${PropertyImagesTable.COLUMN_IS_PRIMARY}', pi.${PropertyImagesTable.COLUMN_IS_PRIMARY},
                                '${PropertyImagesTable.COLUMN_CREATED_AT}', pi.${PropertyImagesTable.COLUMN_CREATED_AT}
                            )
                        ) || ']'
                    ELSE '[]'
                END as images
            FROM ${UserPropertyActionTable.TABLE_NAME} as upa -- Not taking any columns from this table
            JOIN ${PropertyTable.TABLE_NAME} as p ON p.${PropertyTable.COLUMN_ID} = upa.${UserPropertyActionTable.COLUMN_PROPERTY_ID}
            LEFT JOIN ${PropertyImagesTable.TABLE_NAME} as pi ON pi.${PropertyImagesTable.COLUMN_PROPERTY_ID} = p.${PropertyTable.COLUMN_ID}
            WHERE ${UserPropertyActionTable.COLUMN_TENANT_ID} = ? AND ${UserPropertyActionTable.COLUMN_ACTION} = ?
            GROUP BY p.${PropertyTable.COLUMN_ID}
            ORDER BY p.${UserPropertyActionTable.COLUMN_CREATED_AT} DESC
            LIMIT ${pagination.limit} OFFSET ${pagination.offset}
        """.trimIndent()

        val whereArgs = arrayOf(userId.toString(), action.readable)

        readableDB.rawQuery(query, whereArgs).use { cursor ->
            val propertySummaries = mutableListOf<PropertySummaryEntity>()
            while (cursor.moveToNext()) {
                val summaryEntity = PropertyMapper.toPropertySummaryEntity(cursor)
                val imagesJsonString = cursor.getString(cursor.getColumnIndexOrThrow("images"))
                val imagesEntity = PropertyImageMapper.toEntityFromJson(imagesJsonString)

                propertySummaries.add(summaryEntity.copy(images = imagesEntity))
            }

            return propertySummaries
//            it.moveToFirst()
//            println("=== Row ${it.position + 1} ===")
//            for (i in 0 until it.columnCount) {
//                val columnName = it.getColumnName(i)
//                val columnValue = when (it.getType(i)) {
//                    Cursor.FIELD_TYPE_INTEGER -> it.getInt(i).toString()
//                    Cursor.FIELD_TYPE_FLOAT -> it.getFloat(i).toString()
//                    Cursor.FIELD_TYPE_BLOB -> "BLOB[${it.getBlob(i).size} bytes]"
//                    Cursor.FIELD_TYPE_NULL -> "NULL"
//                    else -> it.getString(i) ?: "NULL"
//                }
//                println("$columnName: $columnValue")
//            }
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