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
import com.example.houserentalapp.data.local.db.tables.PropertyTable
import com.example.houserentalapp.data.local.db.tables.UserPropertyActionTable
import com.example.houserentalapp.data.local.db.tables.UserTable
import com.example.houserentalapp.data.mapper.UserMapper
import com.example.houserentalapp.domain.model.Pagination
import com.example.houserentalapp.domain.model.UserPropertyStats
import com.example.houserentalapp.domain.model.enums.LeadStatus
import com.example.houserentalapp.domain.model.enums.UserActionEnum
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

        val id = writableDb.insertWithOnConflict(
            UserPropertyActionTable.TABLE_NAME,
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE
            )

        return id
    }

    fun storeLead(newLeadEntity: NewLeadEntity): Long {
        val values = ContentValues().apply {
            put(LeadTable.COLUMN_TENANT_ID, newLeadEntity.tenantId)
            put(LeadTable.COLUMN_LANDLORD_ID, newLeadEntity.landlordId)
            put(LeadTable.COLUMN_NOTE, newLeadEntity.note)
            put(LeadTable.COLUMN_CREATED_AT, newLeadEntity.createdAt)
        }

        val id = writableDb.insert(LeadTable.TABLE_NAME, null, values)
        if (id == -1L)
            throw SQLException("Failed to insert to LeadTable")

        return id
    }

    fun mapLeadToProperty(leadId: Long, propertyId: Long, status: String): Long {
        val values = ContentValues().apply {
            put(LeadPropertyMappingTable.COLUMN_PROPERTY_ID, propertyId)
            put(LeadPropertyMappingTable.COLUMN_LEAD_ID, leadId)
            put(LeadPropertyMappingTable.COLUMN_STATUS, status)
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
                l.${LeadTable.COLUMN_NOTE} as lead_note,
                l.${LeadTable.COLUMN_CREATED_AT} as lead_created_at,
                u.*,
                GROUP_CONCAT(lpm.${LeadPropertyMappingTable.COLUMN_PROPERTY_ID}) as property_ids,
                GROUP_CONCAT(lpm.${LeadPropertyMappingTable.COLUMN_STATUS}) as lead_property_status
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
            while (cursor.moveToNext())
                leadEntityList.add(parseLeadEntity(cursor))

            return leadEntityList
        }
    }

    fun getLead(leadId: Long): LeadEntity {
        val query = """
            SELECT
                l.${LeadTable.COLUMN_ID} as lead_id,
                l.${LeadTable.COLUMN_TENANT_ID} as lead_tenant_id,
                l.${LeadTable.COLUMN_LANDLORD_ID} as lead_landlord_id,
                l.${LeadTable.COLUMN_NOTE} as lead_note,
                l.${LeadTable.COLUMN_CREATED_AT} as lead_created_at,
                u.*,
                GROUP_CONCAT(lpm.${LeadPropertyMappingTable.COLUMN_PROPERTY_ID}) as property_ids,
                GROUP_CONCAT(lpm.${LeadPropertyMappingTable.COLUMN_STATUS}) as lead_property_status
            FROM ${LeadTable.TABLE_NAME} as l
            JOIN ${UserTable.TABLE_NAME} as u ON l.${LeadTable.COLUMN_TENANT_ID} = u.${UserTable.COLUMN_ID}
            LEFT JOIN ${LeadPropertyMappingTable.TABLE_NAME} as lpm ON l.${LeadTable.COLUMN_ID} = lpm.${LeadPropertyMappingTable.COLUMN_LEAD_ID}
            WHERE l.${LeadTable.COLUMN_ID} = ? 
            GROUP BY l.${LeadTable.COLUMN_ID}
        """.trimIndent()

        readableDB.rawQuery(query, arrayOf(leadId.toString())).use { cursor ->
            if (cursor.moveToNext())
                return parseLeadEntity(cursor)

            throw IllegalArgumentException("Lead not found at the given id: $leadId")
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

    fun getUserPropertyStats(userId: Long): UserPropertyStats {
        val tenantStats =  getTenantActionStats(userId)
        val landlordStats =  getLandlordStats(userId)
        return UserPropertyStats(
            viewedPropertyCount = tenantStats.getOrDefault(UserActionEnum.VIEW.readable, 0),
            shortlistedPropertyCount = tenantStats.getOrDefault(UserActionEnum.SHORTLISTED.readable, 0),
            contactViewedPropertyCount = tenantStats.getOrDefault(UserActionEnum.INTERESTED.readable, 0),
            listedPropertyCount = landlordStats.getOrDefault("propertyCount", 0),
            leadsCount = landlordStats.getOrDefault("leadCount", 0),
        )
    }

    fun getTenantActionStats(tenantId: Long): Map<String, Int> {
        val query = """
            SELECT
                ${UserPropertyActionTable.COLUMN_ACTION},
                COUNT(*) as action_count
            FROM ${UserPropertyActionTable.TABLE_NAME}
            WHERE ${UserPropertyActionTable.COLUMN_TENANT_ID} = $tenantId -- Note Using UserId directly because usually SQL won't happen using int values
            GROUP BY ${UserPropertyActionTable.COLUMN_ACTION}
        """.trimIndent()

        readableDB.rawQuery(query, null).use { cursor ->
            val actionWithCount = mutableMapOf<String, Int>()
            with(cursor) {
                while (cursor.moveToNext())
                    actionWithCount.put(
                        getString(getColumnIndexOrThrow(UserPropertyActionTable.COLUMN_ACTION)),
                        getInt(getColumnIndexOrThrow("action_count"))
                    )
            }
            return actionWithCount
        }
    }

    fun getLandlordStats(landlordId: Long): Map<String, Int> {
        // Get Posted Properties Count and Leads Count
        val propertyCountName = "propertyCount"
        val leadCountName = "leadCount"
        val query = """
            SELECT
                (SELECT COUNT(${PropertyTable.COLUMN_ID})
                FROM ${PropertyTable.TABLE_NAME}
                WHERE ${PropertyTable.COLUMN_LANDLORD_ID} = $landlordId) as $propertyCountName,
                (SELECT COUNT(${LeadTable.COLUMN_ID}) 
                 FROM ${LeadTable.TABLE_NAME} 
                 WHERE ${LeadTable.COLUMN_LANDLORD_ID} = $landlordId) as $leadCountName
        """.trimIndent()

        readableDB.rawQuery(query, null).use { cursor ->
            val countMap = mutableMapOf<String, Int>()
            if (cursor.moveToNext()){
                countMap[propertyCountName] = cursor.getInt(0)
                countMap[leadCountName] = cursor.getInt(1)
            }
            return countMap
        }
    }

    // -------------- UPDATE --------------
    fun updateLeadNote(leadId: Long, newNote: String): Int {
        return writableDb.update(
            LeadTable.TABLE_NAME,
            ContentValues().apply { put(LeadTable.COLUMN_NOTE, newNote) },
            "${LeadTable.COLUMN_ID} = ?",
            arrayOf(leadId.toString())
        )
    }

    fun updateLeadPropertyStatus(leadId: Long, propertyId: Long, staus: LeadStatus): Int {
        return writableDb.update(
            LeadPropertyMappingTable.TABLE_NAME,
            ContentValues().apply { put(LeadPropertyMappingTable.COLUMN_STATUS, staus.readable) },
            "${LeadPropertyMappingTable.COLUMN_LEAD_ID} = ? AND ${LeadPropertyMappingTable.COLUMN_PROPERTY_ID} = ?",
            arrayOf(leadId.toString(), propertyId.toString())
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

    fun parseLeadEntity(cursor: Cursor) = with(cursor) {
        // Get Lead User Info
        val leadUser = UserMapper.toUser(cursor)
        // Combine PropertyId With Status
        val propertyIds = cursor.getString(
            cursor.getColumnIndexOrThrow("property_ids")
        ).let { ids ->
            if (ids.isNullOrEmpty())
                emptyList()
            else
                ids.split(",").map { it.toLong() }
        }
        val leadPropertyStatuses = cursor.getString(
            cursor.getColumnIndexOrThrow("lead_property_status")
        ).let { statuses ->
            if (statuses.isBlank())
                emptyList()
            else
                statuses.split(",").map { LeadStatus.fromString(it) }
        }
        val propertyIdsWithStatus = propertyIds.zip(leadPropertyStatuses)

        LeadEntity(
            id = cursor.getLong(
                cursor.getColumnIndexOrThrow("lead_id")
            ),
            lead = leadUser,
            interestedPropertyIdsWithStatus = propertyIdsWithStatus,
            note = cursor.getStringOrNull(
                cursor.getColumnIndexOrThrow("lead_note")
            ),
            createdAt = cursor.getLong(
                cursor.getColumnIndexOrThrow("lead_created_at")
            )
        )
    }
}