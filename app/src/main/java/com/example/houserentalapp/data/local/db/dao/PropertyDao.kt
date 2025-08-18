package com.example.houserentalapp.data.local.db.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.core.database.getIntOrNull
import com.example.houserentalapp.data.local.db.DatabaseHelper
import com.example.houserentalapp.data.local.db.entity.PropertyAmenityEntity
import com.example.houserentalapp.data.local.db.entity.PropertyEntity
import com.example.houserentalapp.data.local.db.entity.PropertyImageEntity
import com.example.houserentalapp.data.local.db.tables.PropertyAmenitiesTable
import com.example.houserentalapp.data.local.db.tables.PropertyImagesTable
import com.example.houserentalapp.data.local.db.tables.PropertyTable
import java.sql.SQLException
import com.example.houserentalapp.data.local.db.entity.PropertyAddressEntity
import com.example.houserentalapp.data.local.db.entity.PropertySummaryEntity
import com.example.houserentalapp.domain.model.Pagination
import kotlin.jvm.Throws

// Property main table + images + internal amenities + social amenities + etc..
class PropertyDao(private val dbHelper: DatabaseHelper) {
    val writableDB: SQLiteDatabase
        get() = dbHelper.writableDatabase

    val readableDB: SQLiteDatabase
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
            put(PropertyTable.COLUMN_PREFERRED_TENANT_TYPE, entity.preferredTenantType)
            put(PropertyTable.COLUMN_TRANSACTION_TYPE, entity.transactionType)
            put(PropertyTable.COLUMN_AGE_OF_PROPERTY, entity.ageOfProperty)
            put(PropertyTable.COLUMN_COVERED_PARKING, entity.countOfCoveredParking)
            put(PropertyTable.COLUMN_OPEN_PARKING, entity.countOfOpenParking)
            put(PropertyTable.COLUMN_IS_PET_ALLOWED, if (entity.isPetAllowed) 1 else 0)
            put(PropertyTable.COLUMN_AVAILABLE_FROM, entity.availableFrom)
            put(PropertyTable.COLUMN_BHK, entity.bhk)
            put(PropertyTable.COLUMN_BUILT_UP_AREA, entity.builtUpArea)
            put(PropertyTable.COLUMN_BATHROOM_COUNT, entity.bathRoomCount)
            put(PropertyTable.COLUMN_IS_AVAILABLE, if (entity.isAvailable) 1 else 0)
            put(PropertyTable.COLUMN_VIEW_COUNT, entity.viewCount)
            // PRICE
            put(PropertyTable.COLUMN_PRICE, entity.price)
            put(PropertyTable.COLUMN_IS_MAINTENANCE_SEPARATE, if (entity.isMaintenanceSeparate) 1 else 0)
            put(PropertyTable.COLUMN_MAINTENANCE_CHARGES, entity.maintenanceCharges)
            put(PropertyTable.COLUMN_SECURITY_DEPOSIT, entity.numberOfSecurityDepositMonths)
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

    private fun insertPropertyImages(
        db: SQLiteDatabase,
        propertyId: Long,
        images: List<PropertyImageEntity>
    ): List<PropertyImageEntity> {
        images.forEach {
            val values = ContentValues().apply {
                put(PropertyImagesTable.COLUMN_PROPERTY_ID, propertyId)
                put(PropertyImagesTable.COLUMN_IMAGE_ADDRESS, it.imageAddress)
                put(PropertyImagesTable.COLUMN_IS_PRIMARY, if (it.isPrimary) 1 else 0)
            }

            it.id = db.insert(PropertyImagesTable.TABLE_NAME, null, values)
        }

        return images // Return Images with id
    }

    private fun insertAmenities(
        db: SQLiteDatabase,
        propertyId: Long,
        amenities: List<PropertyAmenityEntity>
    ): List<PropertyAmenityEntity> {
        amenities.forEach {
            val values = ContentValues().apply {
                put(PropertyAmenitiesTable.COLUMN_PROPERTY_ID, propertyId)
                put(PropertyAmenitiesTable.COLUMN_AMENITY, it.amenity)
                put(PropertyAmenitiesTable.COLUMN_AMENITY_TYPE, it.amenityType)
                put(PropertyAmenitiesTable.COLUMN_AMENITY, it.amenity)
                put(PropertyAmenitiesTable.COLUMN_COUNT, it.count)
            }

            it.id = db.insert("t_property_amenities", null, values)
        }

        return amenities // Return the amenities with id
    }

    // -------------- READ --------------
    fun getPropertyById(propertyId: Long): PropertyEntity? {
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
            return if (cursor.moveToFirst()) {
                // Read Property
                val property = mapCursorToPropertyEntity(cursor)
                // Read Images
                val images = getPropertyImages(db, propertyId)
                // Read Amenities
                val amenities = getPropertyAmenities(db, propertyId)

                property.copy(images = images, amenities = amenities )
            } else
                null
        }
    }

    fun getPropertySummariesWithFilter(
        filters: Map<String, Any>, pagination: Pagination
    ): List<PropertySummaryEntity> {
        val (whereClause, whereArgs) = buildWhere(filters)
        val orderBy = "${PropertyTable.COLUMN_CREATED_AT} DESC"
        val limit = "${pagination.offset}, ${pagination.limit}"
        val db = readableDB

        db.query(
            PropertyTable.TABLE_NAME,
            null,
            whereClause,
            whereArgs,
            null,
            null,
            orderBy,
            limit
        ).use { cursor ->
            val propertySummaries = mutableListOf<PropertySummaryEntity>()
            with(cursor) {
                while (moveToNext()) {
                    val summary = mapCursorToPropertySummaryEntity(this)
                    // Read Primary Image
                    val primaryImage = getPropertyPrimaryImage(db, summary.id)

                    propertySummaries.add(
                        mapCursorToPropertySummaryEntity(cursor).copy(primaryImage = primaryImage)
                    )
                }
            }

            return propertySummaries
        }
    }

    private fun getPropertyImages(db: SQLiteDatabase, propertyId: Long): List<PropertyImageEntity> {
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
            with(cursor) {
                while (moveToNext()) {
                    images.add(
                        PropertyImageEntity(
                            id = getLong(
                                getColumnIndexOrThrow(
                                    PropertyImagesTable.COLUMN_ID
                                )
                            ),
                            imageAddress = getString(
                                getColumnIndexOrThrow(
                                    PropertyImagesTable.COLUMN_IMAGE_ADDRESS
                                )
                            ),
                            isPrimary = getInt(
                                getColumnIndexOrThrow(
                                    PropertyImagesTable.COLUMN_IS_PRIMARY
                                )
                            ) == 1,
                        )
                    )
                }
            }

            return images
        }
    }

    private fun getPropertyPrimaryImage(db: SQLiteDatabase, propertyId: Long): PropertyImageEntity {
        val whereClause = "${PropertyImagesTable.COLUMN_PROPERTY_ID} = ? AND ${PropertyImagesTable.COLUMN_IS_PRIMARY} = 1"
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
            with(cursor) {
                if (moveToFirst()) {
                    return PropertyImageEntity(
                        id = getLong(
                            getColumnIndexOrThrow(
                                PropertyImagesTable.COLUMN_ID
                            )
                        ),
                        imageAddress = getString(
                            getColumnIndexOrThrow(
                                PropertyImagesTable.COLUMN_IMAGE_ADDRESS
                            )
                        ),
                        isPrimary = getInt(
                            getColumnIndexOrThrow(
                                PropertyImagesTable.COLUMN_IS_PRIMARY
                            )
                        ) == 1,
                    )
                }


            }

            throw SQLException("Primary image is not found for propertyId: $propertyId")
        }
    }

    private fun getPropertyAmenities(db: SQLiteDatabase, propertyId: Long): List<PropertyAmenityEntity> {
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
            val amenities = mutableListOf<PropertyAmenityEntity>()
            with(cursor) {
                while (moveToNext()) {
                    amenities.add(
                        PropertyAmenityEntity(
                            id = getLong(
                                getColumnIndexOrThrow(
                                    PropertyAmenitiesTable.COLUMN_ID
                                )
                            ),
                            amenity = getString(
                                getColumnIndexOrThrow(
                                    PropertyAmenitiesTable.COLUMN_AMENITY
                                )
                            ),
                            amenityType = getString(
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

    private fun buildWhere(
        filters: Map<String, Any>, onlyAvailable: Boolean = true
    ): Pair<String, Array<String>> {
        val clauses = mutableListOf<String>()
        val args = mutableListOf<String>()

        filters.forEach { (key, value) ->
            when(key) {
                "landlordId" -> {
                    clauses.add("${PropertyTable.COLUMN_LANDLORD_ID} = ?")
                    args.add(value.toString())
                }
                "city" -> {
                    clauses.add("${PropertyTable.COLUMN_CITY} = ?")
                    args.add(value.toString())
                }
                "locality" -> {
                    clauses.add("${PropertyTable.COLUMN_LOCALITY} = ?")
                    args.add(value.toString())
                }
                "lookingTo" -> {
                    clauses.add("${PropertyTable.COLUMN_LOOKING_TO} = ?")
                    args.add(value.toString())
                }
                "type" -> {
                    clauses.add("${PropertyTable.COLUMN_TYPE} = ?")
                    args.add(value.toString())
                }
                "bhk" -> {
                    clauses.add("${PropertyTable.COLUMN_BHK} = ?")
                    args.add(value.toString())
                }
                "minPrice" -> {
                    clauses.add("${PropertyTable.COLUMN_PRICE} >= ?")
                    args.add(value.toString())
                }
                "maxPrice" -> {
                    clauses.add("${PropertyTable.COLUMN_PRICE} <= ?")
                    args.add(value.toString())
                }
                "furnishingType" -> {
                    clauses.add("${PropertyTable.COLUMN_FURNISHING_TYPE} = ?")
                    args.add(value.toString())
                }
                "isPetAllowed" -> {
                    clauses.add("${PropertyTable.COLUMN_IS_PET_ALLOWED} = ?")
                    args.add(if (value as Boolean) "1" else "0")
                }
            }
        }

        // Add Condition to get only available products
        if (onlyAvailable)
            clauses.add("${PropertyTable.COLUMN_IS_AVAILABLE} = 1")

        return Pair(
            clauses.joinToString(" AND "),
//            if (clauses.isNotEmpty()) clauses.joinToString(" AND ") else "1",
            args.toTypedArray()
        )
    }

    // -------------- UPDATE --------------
    fun updateProperty(propertyId: Long, updateFields: Map<String, Any>): Int {
        return writableDB.use { db ->
            val values = ContentValues()

            updateFields.forEach { (key, value) ->
                when (key) {
                    "name" -> values.put(PropertyTable.COLUMN_NAME, value as String)
                    "description" -> values.put(PropertyTable.COLUMN_DESCRIPTION, value as String?)
                    "price" -> values.put(PropertyTable.COLUMN_PRICE, value as Int)
                    "isAvailable" -> values.put(PropertyTable.COLUMN_IS_AVAILABLE, if (value as Boolean) 1 else 0)
                    "viewCount" -> values.put(PropertyTable.COLUMN_VIEW_COUNT, value as Int)
                }
            }

            values.put(PropertyTable.COLUMN_MODIFIED_AT, System.currentTimeMillis() / 1000)
            db.update(
                PropertyTable.TABLE_NAME,
                values,
                "${PropertyTable.COLUMN_ID} = ?",
                arrayOf(propertyId.toString())
            )
        }
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
        TODO("need to implement")
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
                preferredTenantType = getString(getColumnIndexOrThrow(PropertyTable.COLUMN_PREFERRED_TENANT_TYPE)),
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
                isAvailable = getInt(getColumnIndexOrThrow(PropertyTable.COLUMN_IS_AVAILABLE)) == 1,
                viewCount = getInt(getColumnIndexOrThrow(PropertyTable.COLUMN_VIEW_COUNT)),
                price = getInt(getColumnIndexOrThrow(PropertyTable.COLUMN_PRICE)),
                isMaintenanceSeparate = getInt(getColumnIndexOrThrow(PropertyTable.COLUMN_IS_MAINTENANCE_SEPARATE)) == 1,
                maintenanceCharges = getIntOrNull(getColumnIndexOrThrow(PropertyTable.COLUMN_MAINTENANCE_CHARGES)),
                numberOfSecurityDepositMonths = getInt(
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

    private fun mapCursorToPropertySummaryEntity(cursor: Cursor): PropertySummaryEntity {
        with(cursor) {
            return PropertySummaryEntity(
                id = getLong(getColumnIndexOrThrow(PropertyTable.COLUMN_ID)),
                name = getString(getColumnIndexOrThrow(PropertyTable.COLUMN_NAME)),
                description = getString(getColumnIndexOrThrow(PropertyTable.COLUMN_DESCRIPTION)),
                lookingTo = getString(getColumnIndexOrThrow(PropertyTable.COLUMN_LOOKING_TO)),
                type = getString(getColumnIndexOrThrow(PropertyTable.COLUMN_TYPE)),
                furnishingType = getString(getColumnIndexOrThrow(PropertyTable.COLUMN_FURNISHING_TYPE)),
                bhk = getString(getColumnIndexOrThrow(PropertyTable.COLUMN_BHK)),
                builtUpArea = getInt(getColumnIndexOrThrow(PropertyTable.COLUMN_BUILT_UP_AREA)),
                viewCount = getInt(getColumnIndexOrThrow(PropertyTable.COLUMN_VIEW_COUNT)),
                price = getInt(getColumnIndexOrThrow(PropertyTable.COLUMN_PRICE)),
                address = PropertyAddressEntity(
                    streetName = getString(getColumnIndexOrThrow(PropertyTable.COLUMN_STREET_NAME)),
                    locality = getString(getColumnIndexOrThrow(PropertyTable.COLUMN_LOCALITY)),
                    city = getString(getColumnIndexOrThrow(PropertyTable.COLUMN_CITY))
                ),
                primaryImage = PropertyImageEntity( // This duplicate value will be replaced
                    null, "", false
                )
            )
        }
    }
}