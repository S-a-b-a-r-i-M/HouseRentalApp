package com.example.houserentalapp.data.local.db.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import androidx.core.database.getIntOrNull
import com.example.houserentalapp.data.local.db.DatabaseHelper
import com.example.houserentalapp.data.local.db.entity.AmenityEntity
import com.example.houserentalapp.data.local.db.entity.PropertyEntity
import com.example.houserentalapp.data.local.db.entity.PropertyImageEntity
import com.example.houserentalapp.data.local.db.tables.PropertyAmenitiesTable
import com.example.houserentalapp.data.local.db.tables.PropertyImagesTable
import com.example.houserentalapp.data.local.db.tables.PropertyTable
import java.sql.SQLException
import com.example.houserentalapp.data.local.db.entity.PropertyAddressEntity
import com.example.houserentalapp.data.local.db.entity.PropertySummaryEntity
import com.example.houserentalapp.data.local.db.tables.UserPropertyActionTable
import com.example.houserentalapp.data.mapper.PropertyImageMapper
import com.example.houserentalapp.data.mapper.PropertyMapper
import com.example.houserentalapp.domain.model.AmenityDomain
import com.example.houserentalapp.domain.model.Pagination
import com.example.houserentalapp.domain.model.Property
import com.example.houserentalapp.domain.model.PropertyFilters
import com.example.houserentalapp.domain.model.enums.PropertyFields
import com.example.houserentalapp.domain.model.enums.ReadableEnum
import com.example.houserentalapp.domain.model.enums.UserActionEnum
import kotlin.jvm.Throws

// Property main table + images + internal amenities + social amenities + etc..
class PropertyDao(private val dbHelper: DatabaseHelper) {
    private val writableDB: SQLiteDatabase
        get() = dbHelper.writableDatabase

    private val readableDB: SQLiteDatabase
        get() = dbHelper.readableDatabase

    // -------------- CREATE --------------

    @Throws(SQLException::class)
    fun insertProperty(entity: PropertyEntity): Long {
        writableDB.use { db ->
            db.beginTransaction()
            try {
                val propertyId = insertPropertyRecord(db, entity)
                insertPropertyImages(db, propertyId, entity.images)
                insertAmenities(db, propertyId, entity.amenities)

                db.setTransactionSuccessful()
                return propertyId
            } finally {
                db.endTransaction()
            }
        }
    }

    private fun insertPropertyRecord(db: SQLiteDatabase, entity: PropertyEntity): Long {
        val values = ContentValues().apply {
            put(PropertyTable.COLUMN_LANDLORD_ID, entity.landlordId)
            put(PropertyTable.COLUMN_NAME, entity.name)
            put(PropertyTable.COLUMN_DESCRIPTION, entity.description)
            put(PropertyTable.COLUMN_LOOKING_TO, entity.lookingTo)
            put(PropertyTable.COLUMN_KIND, entity.kind)
            put(PropertyTable.COLUMN_TYPE, entity.type)
            put(PropertyTable.COLUMN_FURNISHING_TYPE, entity.furnishingType)
            put(PropertyTable.COLUMN_PREFERRED_TENANTS, entity.preferredTenants)
            put(PropertyTable.COLUMN_PREFERRED_BACHELOR_TYPE, entity.preferredBachelorType)
            put(PropertyTable.COLUMN_TRANSACTION_TYPE, entity.transactionType)
            put(PropertyTable.COLUMN_AGE_OF_PROPERTY, entity.ageOfProperty)
            put(PropertyTable.COLUMN_COVERED_PARKING, entity.countOfCoveredParking)
            put(PropertyTable.COLUMN_OPEN_PARKING, entity.countOfOpenParking)
            put(PropertyTable.COLUMN_IS_PET_ALLOWED, if (entity.isPetAllowed) 1 else 0)
            put(PropertyTable.COLUMN_AVAILABLE_FROM, entity.availableFrom)
            put(PropertyTable.COLUMN_BHK, entity.bhk)
            put(PropertyTable.COLUMN_BUILT_UP_AREA, entity.builtUpArea)
            put(PropertyTable.COLUMN_BATHROOM_COUNT, entity.bathRoomCount)
            put(PropertyTable.COLUMN_IS_AVAILABLE, if (entity.isActive) 1 else 0)
            put(PropertyTable.COLUMN_VIEW_COUNT, entity.viewCount)
            // PRICE
            put(PropertyTable.COLUMN_PRICE, entity.price)
            put(PropertyTable.COLUMN_IS_MAINTENANCE_SEPARATE, if (entity.isMaintenanceSeparate) 1 else 0)
            put(PropertyTable.COLUMN_MAINTENANCE_CHARGES, entity.maintenanceCharges)
            put(PropertyTable.COLUMN_SECURITY_DEPOSIT, entity.securityDepositAmount)
            // ADDRESS
            put(PropertyTable.COLUMN_STREET_NAME, entity.address.streetName)
            put(PropertyTable.COLUMN_LOCALITY, entity.address.locality)
            put(PropertyTable.COLUMN_CITY, entity.address.city)
            put(PropertyTable.COLUMN_CREATED_AT, entity.createdAt)
        }

        val propertyId = db.insert(PropertyTable.TABLE_NAME, null, values)
        if (propertyId == -1L)
            throw SQLException("Failed to insert property")

        return propertyId
    }

    fun insertPropertyImages(
        db: SQLiteDatabase = writableDB,
        propertyId: Long,
        images: List<PropertyImageEntity>
    ): List<Long> {
        return images.map {
            val values = ContentValues().apply {
                put(PropertyImagesTable.COLUMN_PROPERTY_ID, propertyId)
                put(PropertyImagesTable.COLUMN_IMAGE_ADDRESS, it.imageAddress)
                put(PropertyImagesTable.COLUMN_IS_PRIMARY, if (it.isPrimary) 1 else 0)
            }

            db.insert(PropertyImagesTable.TABLE_NAME, null, values)
        }
    }

    fun insertAmenities(
        db: SQLiteDatabase = writableDB,
        propertyId: Long,
        amenities: List<AmenityEntity>
    ): List<Long> {
        return amenities.map {
            val values = ContentValues().apply {
                put(PropertyAmenitiesTable.COLUMN_PROPERTY_ID, propertyId)
                put(PropertyAmenitiesTable.COLUMN_AMENITY, it.name)
                put(PropertyAmenitiesTable.COLUMN_AMENITY_TYPE, it.type)
                put(PropertyAmenitiesTable.COLUMN_COUNT, it.count)
            }

            db.insert("t_property_amenities", null, values)
        }
    }

    fun createAmenities(
        db: SQLiteDatabase = writableDB,
        propertyId: Long,
        amenities: List<AmenityDomain>
    ): List<Long> {
        return amenities.map {
            val values = ContentValues().apply {
                put(PropertyAmenitiesTable.COLUMN_PROPERTY_ID, propertyId)
                put(PropertyAmenitiesTable.COLUMN_AMENITY, it.name.readable)
                put(PropertyAmenitiesTable.COLUMN_AMENITY_TYPE, it.type.readable)
                put(PropertyAmenitiesTable.COLUMN_COUNT, it.count)
            }

            db.insert("t_property_amenities", null, values)
        }
    }

    // -------------- READ --------------
    fun getPropertyById(propertyId: Long): PropertyEntity {
        val db = readableDB
        db.query(
            PropertyTable.TABLE_NAME,
            null,
            "${PropertyTable.COLUMN_ID} = ?",
            arrayOf(propertyId.toString()),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                // Read Property
                val property = mapCursorToPropertyEntity(cursor)
                // Read Images
                val images = getPropertyImages(db, propertyId)
                // Read Amenities
                val amenities = getPropertyAmenities(db, propertyId)

                return property.copy(images = images, amenities = amenities )
            }
        }

        throw IllegalArgumentException("property Not found at the given id: $propertyId")
    }

    private fun getPropertySummaryQuery(joinType: String, userId: Long, whereClause: String) : String {
        val pt = PropertyTable
        val pit = PropertyImagesTable
        val upt = UserPropertyActionTable

        return """
            SELECT 
                p.*,
                CASE
                    WHEN COUNT(pi.${pit.COLUMN_ID}) > 0 THEN
                        '[' || GROUP_CONCAT(
                            json_object(
                                '${pit.COLUMN_ID}', pi.${pit.COLUMN_ID},
                                '${pit.COLUMN_IMAGE_ADDRESS}', pi.${pit.COLUMN_IMAGE_ADDRESS},
                                '${pit.COLUMN_IS_PRIMARY}', pi.${pit.COLUMN_IS_PRIMARY},
                                '${pit.COLUMN_CREATED_AT}', pi.${pit.COLUMN_CREATED_AT}
                            )
                        ) || ']'
                    ELSE '[]'
                END as images,
                CASE 
                    WHEN MAX(upa.${upt.COLUMN_ID}) IS NOT NULL THEN 1 ELSE 0 
                END as isShortlisted
            FROM ${pt.TABLE_NAME} as p
            LEFT JOIN ${pit.TABLE_NAME} as pi ON pi.${pit.COLUMN_PROPERTY_ID} = p.${pt.COLUMN_ID}
            $joinType ${upt.TABLE_NAME} as upa
                ON upa.${upt.COLUMN_TENANT_ID} = $userId AND 
                   upa.${upt.COLUMN_PROPERTY_ID} = p.${pt.COLUMN_ID} AND
                   upa.${upt.COLUMN_ACTION} = '${UserActionEnum.SHORTLISTED.readable}' 
            WHERE $whereClause
            GROUP BY p.${pt.COLUMN_ID}
        """.trimIndent()
    }

    fun getPropertySummariesWithFilter(
        userId: Long,
        pagination: Pagination,
        filters: PropertyFilters?
    ): List<Pair<PropertySummaryEntity, Boolean>> {
        val p = PropertyTable.TABLE_NAME
        val pi = PropertyImagesTable.TABLE_NAME
        val upa = UserPropertyActionTable.TABLE_NAME

        var joinType = "LEFT JOIN"
        var orderBy = "$p.${PropertyTable.COLUMN_CREATED_AT} DESC"

        var whereConditions = ""
        var whereArgs = arrayOf<String>()
        if (filters != null) {
            buildWhere(filters, userId).let { (conditions, args) ->
                whereConditions = conditions
                whereArgs = args
            }

            if (filters.onlyShortlisted) {
                joinType = "JOIN"
                orderBy = "$upa.${UserPropertyActionTable.COLUMN_CREATED_AT} DESC"
            }
        } else { // Fetch Excepts Current User's Property
            whereConditions = "${PropertyTable.COLUMN_LANDLORD_ID}  != ?"
            whereArgs = arrayOf(userId.toString())
        }


        val query = """
            SELECT 
                $p.*,
                CASE
                    WHEN COUNT($pi.${PropertyTable.COLUMN_ID}) > 0 THEN
                        '[' || GROUP_CONCAT(
                            json_object(
                                '${PropertyImagesTable.COLUMN_ID}', $pi.${PropertyImagesTable.COLUMN_ID},
                                '${PropertyImagesTable.COLUMN_IMAGE_ADDRESS}', $pi.${PropertyImagesTable.COLUMN_IMAGE_ADDRESS},
                                '${PropertyImagesTable.COLUMN_IS_PRIMARY}', $pi.${PropertyImagesTable.COLUMN_IS_PRIMARY},
                                '${PropertyImagesTable.COLUMN_CREATED_AT}', $pi.${PropertyImagesTable.COLUMN_CREATED_AT}
                            )
                        ) || ']'
                    ELSE '[]'
                END as images,
                CASE 
                    WHEN MAX($upa.${UserPropertyActionTable.COLUMN_ID}) IS NOT NULL THEN 1 ELSE 0 
                END as isShortlisted
            FROM $p
            LEFT JOIN $pi ON $pi.${PropertyImagesTable.COLUMN_PROPERTY_ID} = $p.${PropertyTable.COLUMN_ID}
            $joinType $upa
                ON $upa.${UserPropertyActionTable.COLUMN_TENANT_ID} = $userId AND 
                   $upa.${UserPropertyActionTable.COLUMN_PROPERTY_ID} = $p.${PropertyTable.COLUMN_ID} AND
                   $upa.${UserPropertyActionTable.COLUMN_ACTION} = '${UserActionEnum.SHORTLISTED.readable}' 
            WHERE $whereConditions
            GROUP BY $p.${PropertyTable.COLUMN_ID}
            ORDER BY $orderBy
            LIMIT ${pagination.limit} OFFSET ${pagination.offset}
        """.trimIndent()

        /* close cursors after use because it's hold native resources that won't be garbage collected. */
        readableDB.rawQuery(query, whereArgs).use { cursor ->
            val result = mutableListOf<Pair<PropertySummaryEntity, Boolean>>()
            while (cursor.moveToNext()) {
                val summaryEntity = PropertyMapper.toPropertySummaryEntity(cursor)
                val imagesJsonString = cursor.getString(cursor.getColumnIndexOrThrow("images"))
                val imagesEntity = PropertyImageMapper.toEntityFromJson(imagesJsonString)
                val isShortListed = cursor.getInt(cursor.getColumnIndexOrThrow("isShortlisted")) == 1

                result.add(Pair(summaryEntity.copy(images = imagesEntity), isShortListed))
            }

            return result
        }
    }

    fun getPropertySummaryWithAction(
        userId: Long, propertyId: Long
    ): Pair<PropertySummaryEntity, Boolean> {
        val joinType = "LEFT JOIN"
        val whereClause = "p.${PropertyTable.COLUMN_ID} = ?"
        val query = getPropertySummaryQuery(joinType, userId, whereClause)

        /* close cursors after use because it's hold native resources that won't be garbage collected. */
        readableDB.rawQuery(query, arrayOf(propertyId.toString())).use { cursor ->
            if (cursor.moveToFirst()) {
                val summaryEntity = PropertyMapper.toPropertySummaryEntity(cursor)
                val imagesJsonString = cursor.getString(cursor.getColumnIndexOrThrow("images"))
                val imagesEntity = PropertyImageMapper.toEntityFromJson(imagesJsonString)
                val isShortListed = cursor.getInt(cursor.getColumnIndexOrThrow("isShortlisted")) == 1

                return Pair(summaryEntity.copy(images = imagesEntity), isShortListed)
            }

            throw IllegalArgumentException("property Not found at the given id: $propertyId")
        }
    }

    fun getPropertySummariesById(propertyIds: List<Long>): List<PropertySummaryEntity> {
        val query = """
            SELECT 
                p.*,
                CASE
                    WHEN COUNT(pi.${PropertyTable.COLUMN_ID}) > 0 THEN
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
            FROM ${PropertyTable.TABLE_NAME} as p
            LEFT JOIN ${PropertyImagesTable.TABLE_NAME} as pi ON pi.${PropertyImagesTable.COLUMN_PROPERTY_ID} = p.${PropertyTable.COLUMN_ID} 
            WHERE p.${PropertyTable.COLUMN_ID} IN (${propertyIds.joinToString(",") { "?" }})
            GROUP BY p.${PropertyTable.COLUMN_ID}
        """.trimIndent()

        /* close cursors after use because it's hold native resources that won't be garbage collected. */
        readableDB.rawQuery(
            query,
            arrayOf<String>() + propertyIds.map { it.toString() }
        ).use { cursor ->
            val result = mutableListOf<PropertySummaryEntity>()
            while (cursor.moveToNext()) {
                val summaryEntity = PropertyMapper.toPropertySummaryEntity(cursor)
                val imagesJsonString = cursor.getString(cursor.getColumnIndexOrThrow("images"))
                val imagesEntity = PropertyImageMapper.toEntityFromJson(imagesJsonString)

                result.add(summaryEntity.copy(images = imagesEntity))
            }

            return result
        }
    }

    private fun getPropertiesCount(
        db: SQLiteDatabase, whereClause: String, whereArgs: Array<String>
    ): Int {
//        readableDB.query(
//            PropertyTable.TABLE_NAME,
//            arrayOf("COUNT(*)"),
//            whereClause,
//            whereArgs,
//            null, null, null, null
//        ).use { cursor ->
//            if(cursor.moveToFirst())
//                return cursor.getInt(0)
//        }
//
//        return -1

        return DatabaseUtils.queryNumEntries(
            db,
            PropertyTable.TABLE_NAME,
            whereClause,
            whereArgs
        ).toInt()
    }

    fun getPropertyImages(db: SQLiteDatabase = writableDB, propertyId: Long): List<PropertyImageEntity> {
        val whereClause = "${PropertyImagesTable.COLUMN_PROPERTY_ID} = ?"
        val whereValue = arrayOf(propertyId.toString())

        db.query(
            PropertyImagesTable.TABLE_NAME,
            null,
            whereClause,
            whereValue,
            null,
            null,
            null
        ).use { cursor ->
            val images = mutableListOf<PropertyImageEntity>()
            while (cursor.moveToNext())
                images.add(PropertyImageMapper.toEntity(cursor))

            return images
        }
    }

    fun getPropertyAmenities(db: SQLiteDatabase = writableDB, propertyId: Long): List<AmenityEntity> {
        val whereClause = "${PropertyAmenitiesTable.COLUMN_PROPERTY_ID} = ?"
        val whereValue = arrayOf(propertyId.toString())

        db.query(
            PropertyAmenitiesTable.TABLE_NAME,
            null,
            whereClause,
            whereValue,
            null,
            null,
            null
        ).use { cursor ->
            val amenities = mutableListOf<AmenityEntity>()
            with(cursor) {
                while (moveToNext()) {
                    amenities.add(
                        AmenityEntity(
                            id = getLong(
                                getColumnIndexOrThrow(
                                    PropertyAmenitiesTable.COLUMN_ID
                                )
                            ),
                            name = getString(
                                getColumnIndexOrThrow(
                                    PropertyAmenitiesTable.COLUMN_AMENITY
                                )
                            ),
                            type = getString(
                                getColumnIndexOrThrow(
                                    PropertyAmenitiesTable.COLUMN_AMENITY_TYPE
                                )
                            ),
                            count = getIntOrNull(
                                getColumnIndexOrThrow(
                                    PropertyAmenitiesTable.COLUMN_COUNT
                                )
                            )
                        )
                    )
                }
            }

            return amenities
        }
    }

    private fun buildWhere(filters: PropertyFilters, userId: Long): Pair<String, Array<String>> {
        val clauses = mutableListOf<String>()
        val args = mutableListOf<String>()

        // User Based
        clauses.add(
            PropertyTable.COLUMN_LANDLORD_ID +
            if (filters.onlyUserProperties) " = ?" else " != ?"
        )
        args.add(userId.toString())

        if (filters.searchQuery.isNotBlank()) {
            clauses.add("(LOWER(${PropertyTable.COLUMN_CITY}) LIKE LOWER(?) OR LOWER(${PropertyTable.COLUMN_LOCALITY}) LIKE LOWER(?))")
            args.add("%${filters.searchQuery}%")
            args.add("%${filters.searchQuery}%")
        }

        fun applyQueryForListOfValues(columnName: String, options: List<ReadableEnum>) {
            val placeHolder = options.joinToString(",") { "?" }
            clauses.add("LOWER($columnName) IN ($placeHolder)")
            args.addAll(options.map { it.readable.lowercase() })
        }

        if (filters.bhkTypes.isNotEmpty())
            applyQueryForListOfValues(
                PropertyTable.COLUMN_BHK,
                filters.bhkTypes
            )

        if (filters.propertyTypes.isNotEmpty())
            applyQueryForListOfValues(
                PropertyTable.COLUMN_TYPE,
                filters.propertyTypes
            )

        if (filters.furnishingTypes.isNotEmpty())
            applyQueryForListOfValues(
                PropertyTable.COLUMN_FURNISHING_TYPE,
                filters.furnishingTypes
            )

        if (filters.tenantTypes.isNotEmpty())
            applyQueryForListOfValues(
                PropertyTable.COLUMN_PREFERRED_TENANTS,
                filters.tenantTypes
            )

        filters.budget?.let { (min, max) ->
            clauses.add("${PropertyTable.COLUMN_PRICE} BETWEEN ? AND ?")
            args.add(min.toString())
            args.add(max.toString())
        }

        // Add Condition to get only available products
        if (filters.onlyAvailable)
            clauses.add("${PropertyTable.COLUMN_IS_AVAILABLE} = 1")

        val joinedWhere = if (clauses.isNotEmpty()) clauses.joinToString(" AND ") else " 1 = 1 "
        return Pair(joinedWhere, args.toTypedArray())
    }

    // -------------- UPDATE --------------
    fun updateProperty(property: Property, updatedFields: List<PropertyFields>): Int {
        val values = ContentValues()
        updatedFields.forEach { field ->
            when (field) {
                PropertyFields.NAME -> values.put(PropertyTable.COLUMN_NAME, property.name)
                PropertyFields.DESCRIPTION -> values.put(PropertyTable.COLUMN_DESCRIPTION, property.description)
                PropertyFields.KIND -> values.put(PropertyTable.COLUMN_KIND, property.kind.readable)
                PropertyFields.TYPE -> values.put(PropertyTable.COLUMN_TYPE, property.type.readable)
                PropertyFields.FURNISHING_TYPE -> values.put(PropertyTable.COLUMN_FURNISHING_TYPE, property.furnishingType.readable)
                PropertyFields.PREFERRED_TENANT_TYPE -> values.put(PropertyTable.COLUMN_PREFERRED_TENANTS, property.preferredTenantType.joinToString(","))
                PropertyFields.PREFERRED_BACHELOR_TYPE -> values.put(PropertyTable.COLUMN_PREFERRED_BACHELOR_TYPE, property.preferredBachelorType?.readable)
                PropertyFields.BATH_ROOM_COUNT -> values.put(PropertyTable.COLUMN_BATHROOM_COUNT, property.bathRoomCount)
                PropertyFields.COVERED_PARKING_COUNT -> values.put(PropertyTable.COLUMN_COVERED_PARKING, property.countOfCoveredParking)
                PropertyFields.OPEN_PARKING_COUNT -> values.put(PropertyTable.COLUMN_OPEN_PARKING, property.countOfOpenParking)
                PropertyFields.AVAILABLE_FROM -> values.put(PropertyTable.COLUMN_AVAILABLE_FROM, property.availableFrom)
                PropertyFields.BHK -> values.put(PropertyTable.COLUMN_BHK, property.bhk.readable)
                PropertyFields.BUILT_UP_AREA -> values.put(PropertyTable.COLUMN_BUILT_UP_AREA, property.builtUpArea)
                PropertyFields.IS_PET_FRIENDLY -> values.put(PropertyTable.COLUMN_IS_PET_ALLOWED, if (property.isPetAllowed) 1 else 0)
                PropertyFields.PRICE -> values.put(PropertyTable.COLUMN_PRICE, property.price)
                PropertyFields.IS_MAINTENANCE_SEPARATE -> values.put(PropertyTable.COLUMN_IS_MAINTENANCE_SEPARATE, property.isMaintenanceSeparate)
                PropertyFields.MAINTENANCE_CHARGES -> values.put(PropertyTable.COLUMN_MAINTENANCE_CHARGES, property.maintenanceCharges)
                PropertyFields.SECURITY_DEPOSIT -> values.put(PropertyTable.COLUMN_SECURITY_DEPOSIT, property.securityDepositAmount)
                PropertyFields.CITY -> values.put(PropertyTable.COLUMN_CITY, property.address.city)
                PropertyFields.STREET -> values.put(PropertyTable.COLUMN_STREET_NAME, property.address.street)
                PropertyFields.LOCALITY -> values.put(PropertyTable.COLUMN_LOCALITY, property.address.locality)
                PropertyFields.AMENITIES -> { }
                PropertyFields.IMAGES -> { }
            }
        }

        values.put(PropertyTable.COLUMN_MODIFIED_AT, System.currentTimeMillis() / 1000)
        return writableDB.update(
            PropertyTable.TABLE_NAME,
            values,
            "${PropertyTable.COLUMN_ID} = ?",
            arrayOf(property.id.toString())
        )
    }

    fun updatePropertyAvailability(propertyId: Long, isAvailable: Boolean): Int {
        val values = ContentValues().apply {
            put(PropertyTable.COLUMN_IS_AVAILABLE, isAvailable)
            put(PropertyTable.COLUMN_MODIFIED_AT, System.currentTimeMillis() / 1000)
        }

        return writableDB.update(
            PropertyTable.TABLE_NAME,
            values,
            "${PropertyTable.COLUMN_ID} = ?",
            arrayOf(propertyId.toString())
        )
    }

    fun updateAmenities(amenities: List<AmenityDomain>): Int {
        var updatedRows = 0
        amenities.forEach {
            updatedRows += writableDB.update(
                PropertyAmenitiesTable.TABLE_NAME,
                ContentValues().apply { put(PropertyAmenitiesTable.COLUMN_COUNT, it.count) },
                "${PropertyAmenitiesTable.COLUMN_ID} = ?",
                arrayOf(it.id.toString())
            )
        }
        return updatedRows
    }

    fun incrementViewCount(propertyId: Long): Int {
        return writableDB.use { db ->
            db.execSQL(
                """
                    |UPDATE ${PropertyTable.TABLE_NAME} 
                    |SET ${PropertyTable.COLUMN_VIEW_COUNT} = ${PropertyTable.COLUMN_VIEW_COUNT} + 1 
                    |WHERE ${PropertyTable.COLUMN_ID} = ?
                    |""".trimMargin(),
                arrayOf(propertyId.toString())
            )
            1
        }
    }

    // -------------- DELETE --------------
    fun deleteProperty(propertyId: Long, landlordId: Long): Int {
        return writableDB.delete(
            PropertyTable.TABLE_NAME,
            "${PropertyTable.COLUMN_ID} = ? AND ${PropertyTable.COLUMN_LANDLORD_ID} = ?",
            arrayOf(propertyId.toString(), landlordId.toString())
        )
    }

    fun deleteAmenities(propertyId: Long, amenityIds: List<Long>): Int {
        val placeHolder = amenityIds.joinToString(",") { "?" }
        return writableDB.delete(
            PropertyAmenitiesTable.TABLE_NAME,
            "${PropertyAmenitiesTable.COLUMN_ID} IN ($placeHolder)",
            arrayOf<String>() + amenityIds.map { it.toString() }
        )
    }

    fun deletePropertyImages(imageIds: List<Long>): Int {
        val placeHolder = imageIds.joinToString(",") { "?" }
        return writableDB.delete(
            PropertyImagesTable.TABLE_NAME,
            "${PropertyImagesTable.COLUMN_ID} IN ($placeHolder)",
            arrayOf<String>() + imageIds.map { it.toString() }
        )
    }

    // ------------ MAPPERS -------------

    private fun mapCursorToPropertyEntity(cursor: Cursor): PropertyEntity {
        with(cursor) {
            return PropertyEntity(
                id = getLong(getColumnIndexOrThrow(PropertyTable.COLUMN_ID)),
                landlordId = getLong(getColumnIndexOrThrow(PropertyTable.COLUMN_LANDLORD_ID)),
                name = getString(getColumnIndexOrThrow(PropertyTable.COLUMN_NAME)),
                description = getString(getColumnIndexOrThrow(PropertyTable.COLUMN_DESCRIPTION)),
                lookingTo = getString(getColumnIndexOrThrow(PropertyTable.COLUMN_LOOKING_TO)),
                kind = getString(getColumnIndexOrThrow(PropertyTable.COLUMN_KIND)),
                type = getString(getColumnIndexOrThrow(PropertyTable.COLUMN_TYPE)),
                furnishingType = getString(getColumnIndexOrThrow(PropertyTable.COLUMN_FURNISHING_TYPE)),
                amenities = emptyList(), // Will be populated separately
                preferredTenants = getString(getColumnIndexOrThrow(PropertyTable.COLUMN_PREFERRED_TENANTS)),
                preferredBachelorType = getString(getColumnIndexOrThrow(PropertyTable.COLUMN_PREFERRED_BACHELOR_TYPE)),
                transactionType = getString(getColumnIndexOrThrow(PropertyTable.COLUMN_TRANSACTION_TYPE)),
                ageOfProperty = getIntOrNull(getColumnIndexOrThrow(PropertyTable.COLUMN_AGE_OF_PROPERTY)),
                countOfCoveredParking = getInt(getColumnIndexOrThrow(PropertyTable.COLUMN_COVERED_PARKING)),
                countOfOpenParking = getInt(getColumnIndexOrThrow(PropertyTable.COLUMN_OPEN_PARKING)),
                availableFrom = getLong(getColumnIndexOrThrow(PropertyTable.COLUMN_AVAILABLE_FROM)),
                bhk = getString(getColumnIndexOrThrow(PropertyTable.COLUMN_BHK)),
                builtUpArea = getInt(getColumnIndexOrThrow(PropertyTable.COLUMN_BUILT_UP_AREA)),
                bathRoomCount = getInt(getColumnIndexOrThrow(PropertyTable.COLUMN_BATHROOM_COUNT)),
                isPetAllowed = getInt(getColumnIndexOrThrow(PropertyTable.COLUMN_IS_PET_ALLOWED)) == 1,
                isActive = getInt(getColumnIndexOrThrow(PropertyTable.COLUMN_IS_AVAILABLE)) == 1,
                viewCount = getInt(getColumnIndexOrThrow(PropertyTable.COLUMN_VIEW_COUNT)),
                price = getInt(getColumnIndexOrThrow(PropertyTable.COLUMN_PRICE)),
                isMaintenanceSeparate = getInt(getColumnIndexOrThrow(PropertyTable.COLUMN_IS_MAINTENANCE_SEPARATE)) == 1,
                maintenanceCharges = getIntOrNull(getColumnIndexOrThrow(PropertyTable.COLUMN_MAINTENANCE_CHARGES)),
                securityDepositAmount = getInt(
                    getColumnIndexOrThrow(
                        PropertyTable.COLUMN_SECURITY_DEPOSIT
                    )
                ),
                address = PropertyAddressEntity(
                    streetName = getString(getColumnIndexOrThrow(PropertyTable.COLUMN_STREET_NAME)),
                    locality = getString(getColumnIndexOrThrow(PropertyTable.COLUMN_LOCALITY)),
                    city = getString(getColumnIndexOrThrow(PropertyTable.COLUMN_CITY))
                ),
                images = emptyList(), // Will be populated separately
                createdAt = getLong(getColumnIndexOrThrow(PropertyTable.COLUMN_CREATED_AT))
            )
        }
    }
}