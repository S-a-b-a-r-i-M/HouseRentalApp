package com.example.houserentalapp.data.local.db.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.core.database.getStringOrNull
import com.example.houserentalapp.data.local.db.DatabaseHelper
import com.example.houserentalapp.data.local.db.entity.LeadEntity
import com.example.houserentalapp.data.local.db.entity.NewLeadEntity
import com.example.houserentalapp.data.local.db.entity.UserActionEntity
import com.example.houserentalapp.data.local.db.tables.LeadPropertyMappingTable
import com.example.houserentalapp.data.local.db.tables.LeadTable
import com.example.houserentalapp.data.local.db.tables.UserPropertyActionTable
import com.example.houserentalapp.data.local.db.tables.UserTable
import com.example.houserentalapp.data.mapper.UserMapper
import com.example.houserentalapp.domain.model.Pagination
import com.example.houserentalapp.domain.model.enums.LeadUpdatableField
import com.example.houserentalapp.domain.model.enums.UserActionEnum
import com.example.houserentalapp.presentation.utils.extensions.logWarning
import java.sql.SQLException

// User and Properties Relation
class UserPropertyDao(private val dbHelper: DatabaseHelper) {
    private val writableDb: SQLiteDatabase
        get() = dbHelper.writableDatabase

    private val readableDB: SQLiteDatabase
        get() = dbHelper.readableDatabase

    // -------------- CREATE --------------
    fun storeUserAction(userId: Long, propertyId: Long, action: UserActionEnum): Long {
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

    fun storeLead(newLeadEntity: NewLeadEntity): Long {
        val values = ContentValues().apply {
            put(LeadTable.COLUMN_TENANT_ID, newLeadEntity.tenantId)
            put(LeadTable.COLUMN_LANDLORD_ID, newLeadEntity.landlordId)
            put(LeadTable.COLUMN_STATUS, newLeadEntity.status)
            put(LeadTable.COLUMN_NOTE, newLeadEntity.note)
            put(LeadTable.COLUMN_CREATED_AT, newLeadEntity.createdAt)
        }

        val id = writableDb.insert(LeadTable.TABLE_NAME, null, values)
        if (id == -1L)
            throw SQLException("Failed to insert to LeadTable")

        return id
    }

    fun mapLeadToProperty(leadId: Long, propertyId: Long): Long {
        val values = ContentValues().apply {
            put(LeadPropertyMappingTable.COLUMN_PROPERTY_ID, propertyId)
            put(LeadPropertyMappingTable.COLUMN_LEAD_ID, leadId)
            put(LeadPropertyMappingTable.COLUMN_CREATED_AT, System.currentTimeMillis())
        }

        val id = writableDb.insert(LeadPropertyMappingTable.TABLE_NAME, null, values)
        if (id == -1L)
            throw SQLException("Failed to insert to mapLeadToProperty")

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

    fun getLeadId(landlordId: Long, tenantId: Long) : Long? {
        val whereClause = """
            ${LeadTable.COLUMN_LANDLORD_ID} = ? AND
            ${LeadTable.COLUMN_TENANT_ID} = ?
        """.trimIndent()
        val whereArgs = arrayOf(landlordId.toString(), tenantId.toString())

        readableDB.query(
            LeadTable.TABLE_NAME,
            arrayOf(LeadTable.COLUMN_ID),
            whereClause,
            whereArgs,
            null, null, null,
        ).use { cursor ->
            if (cursor.moveToFirst())
                return cursor.getLong(
                    cursor.getColumnIndexOrThrow(LeadTable.COLUMN_ID)
                )

            return null
        }
    }

    fun getLeads(landlordId: Long, pagination: Pagination): List<LeadEntity> {
        val query = """
            SELECT 
                l.${LeadTable.COLUMN_ID} as lead_id,
                l.${LeadTable.COLUMN_TENANT_ID} as lead_tenant_id,
                l.${LeadTable.COLUMN_LANDLORD_ID} as lead_landlord_id,
                l.${LeadTable.COLUMN_STATUS} as lead_status,
                l.${LeadTable.COLUMN_NOTE} as lead_note,
                l.${LeadTable.COLUMN_CREATED_AT} as lead_created_at,
                u.*,
                GROUP_CONCAT(lpm.${LeadPropertyMappingTable.COLUMN_PROPERTY_ID}) as property_ids
            FROM ${LeadTable.TABLE_NAME} as l
            JOIN ${UserTable.TABLE_NAME} as u ON l.${LeadTable.COLUMN_TENANT_ID} = u.${UserTable.COLUMN_ID}
            LEFT JOIN ${LeadPropertyMappingTable.TABLE_NAME} as lpm ON l.${LeadTable.COLUMN_ID} = lpm.${LeadPropertyMappingTable.COLUMN_LEAD_ID}
            WHERE l.${LeadTable.COLUMN_LANDLORD_ID} = ? 
            GROUP BY l.${LeadTable.COLUMN_ID}
            ORDER BY l.${LeadTable.COLUMN_CREATED_AT} DESC
            LIMIT ${pagination.limit} OFFSET ${pagination.offset}
        """.trimIndent()

        readableDB.rawQuery(query, arrayOf(landlordId.toString())).use { cursor ->
            val leadEntityList = mutableListOf<LeadEntity>()
            while (cursor.moveToNext()) {
                val tenantUser = UserMapper.toUser(cursor)
                val propertyIds = cursor.getString(
                    cursor.getColumnIndexOrThrow("property_ids")
                ).let { ids ->
                    if (ids.isNullOrEmpty())
                        emptyList()
                    else
                        ids.split(",").map { it.toLong() }
                }

                leadEntityList.add(
                    LeadEntity(
                        id = cursor.getLong(
                            cursor.getColumnIndexOrThrow("lead_id")
                        ),
                        lead = tenantUser,
                        interestedPropertyIds = propertyIds,
                        status = cursor.getString(
                            cursor.getColumnIndexOrThrow("lead_status")
                        ),
                        note = cursor.getStringOrNull(
                            cursor.getColumnIndexOrThrow("lead_note")
                        ),
                        createdAt = cursor.getLong(
                            cursor.getColumnIndexOrThrow("lead_created_at")
                        )
                    )
                )
            }

            return leadEntityList
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
    fun updateLead(leadId: Long, updateData: Map<LeadUpdatableField, String>): Int {
        val values = ContentValues()
        updateData.forEach { (k, v) ->
            when(k) {
                LeadUpdatableField.STATUS -> values.put(LeadTable.COLUMN_STATUS, v)
                LeadUpdatableField.NOTE -> values.put(LeadTable.COLUMN_NOTE, v)
            }
        }

        if (values.isEmpty) {
            logWarning("updateLead: Content Values are empty")
            return 0
        }

        return writableDb.update(
            LeadTable.TABLE_NAME,
            values,
            "${LeadTable.COLUMN_ID} = ?",
            arrayOf(leadId.toString())
        )
    }


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