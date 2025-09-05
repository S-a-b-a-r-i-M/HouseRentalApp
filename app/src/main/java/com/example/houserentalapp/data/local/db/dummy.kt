package com.example.houserentalapp.data.local.db

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.houserentalapp.data.local.db.entity.PropertyAddressEntity
import com.example.houserentalapp.data.local.db.entity.PropertyAmenityEntity
import com.example.houserentalapp.data.local.db.entity.PropertyEntity
import com.example.houserentalapp.data.local.db.tables.PropertyAmenitiesTable
import com.example.houserentalapp.data.local.db.tables.PropertyImagesTable
import com.example.houserentalapp.data.local.db.tables.PropertyTable
import com.example.houserentalapp.data.local.db.tables.UserTable
import com.example.houserentalapp.domain.model.enums.AmenityType
import com.example.houserentalapp.domain.model.enums.BHK
import com.example.houserentalapp.domain.model.enums.CountableInternalAmenity
import com.example.houserentalapp.domain.model.enums.FurnishingType
import com.example.houserentalapp.domain.model.enums.InternalAmenity
import com.example.houserentalapp.domain.model.enums.LookingTo
import com.example.houserentalapp.domain.model.enums.PropertyKind
import com.example.houserentalapp.domain.model.enums.PropertyType
import com.example.houserentalapp.domain.model.enums.SocialAmenity
import com.example.houserentalapp.domain.model.enums.TenantType
import java.sql.SQLException

fun getDummyPropertyEntity(index: Int) =
    PropertyEntity(
        id = null,
        landlordId = 1,
        name = listOf("Hulk Home", "Tony Stark Home", "Captain Home", "Black Widow Home", "Thor Home").random() + index,
        description = "Description is a brief way to tell about your house.",
        lookingTo = LookingTo.RENT.readable,
        kind = PropertyKind.RESIDENTIAL.readable,
        type = PropertyType.entries.random().readable,
        furnishingType = FurnishingType.entries.random().readable,
        amenities = listOf(
            PropertyAmenityEntity(
                id = null,
                name = CountableInternalAmenity.entries.random().readable,
                type = AmenityType.INTERNAL_COUNTABLE.readable,
                count = 2
            ),
            PropertyAmenityEntity(
                id = null,
                name = InternalAmenity.entries.random().readable,
                type = AmenityType.INTERNAL.readable,
                count = null
            ),
            PropertyAmenityEntity(
                id = null,
                name = SocialAmenity.entries.random().readable,
                type = AmenityType.SOCIAL.readable,
                count = null
            ),
        ),
        preferredTenants = TenantType.entries.random().readable,
        preferredBachelorType = null,
        transactionType = null,
        ageOfProperty = null,
        countOfCoveredParking = 1,
        countOfOpenParking = 1,
        availableFrom = System.currentTimeMillis(),
        bhk = BHK.entries.random().readable,
        builtUpArea = 1250,
        bathRoomCount = 2,
        isPetAllowed = listOf(true, false).random(),
        isAvailable = true,
        viewCount = 0,
        price = listOf(6000, 10000, 20000, 30000, 40000, 14000).random(),
        isMaintenanceSeparate = true,
        maintenanceCharges = 1200,
        securityDepositAmount = listOf(60000, 40000).random(),
        address = PropertyAddressEntity(
            streetName = "1/10, StreetName",
            locality = listOf("tambaram", "sri rangam", "vannemalai", "ecr", "kodambakkam", "air port").random(),
            city = listOf("trichy", "karur", "erode", "salem", "namakkal", "chennai").random()
        ),
        images = emptyList(),
        createdAt = System.currentTimeMillis()
    )

fun insertUsers(db: SQLiteDatabase): Long {

    var values = ContentValues().apply {
        put(UserTable.COLUMN_NAME, "Owner")
        put(UserTable.COLUMN_EMAIL, "owner@gmail.com")
        put(UserTable.COLUMN_PHONE, "9878089777")
        put(UserTable.COLUMN_HASHED_PASSWORD, "8hiuasf7y")
    }

    return db.insertOrThrow(UserTable.TABLE_NAME, null, values)
}

fun insertInitialData(db: SQLiteDatabase) {
    val userId = insertUsers(db)

    for (i in 0..1500) {
        val entity = getDummyPropertyEntity(i)
        val values = ContentValues().apply {
            put(PropertyTable.COLUMN_LANDLORD_ID, userId)
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
            put(PropertyTable.COLUMN_IS_AVAILABLE, if (entity.isAvailable) 1 else 0)
            put(PropertyTable.COLUMN_VIEW_COUNT, entity.viewCount)
            // PRICE
            put(PropertyTable.COLUMN_PRICE, entity.price)
            put(
                PropertyTable.COLUMN_IS_MAINTENANCE_SEPARATE,
                if (entity.isMaintenanceSeparate) 1 else 0
            )
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

        entity.images.forEach {
            val values = ContentValues().apply {
                put(PropertyImagesTable.COLUMN_PROPERTY_ID, propertyId)
                put(PropertyImagesTable.COLUMN_IMAGE_ADDRESS, it.imageAddress)
                put(PropertyImagesTable.COLUMN_IS_PRIMARY, if (it.isPrimary) 1 else 0)
            }

            it.id = db.insert(PropertyImagesTable.TABLE_NAME, null, values)
        }

        entity.amenities.forEach {
            val values = ContentValues().apply {
                put(PropertyAmenitiesTable.COLUMN_PROPERTY_ID, propertyId)
                put(PropertyAmenitiesTable.COLUMN_AMENITY, it.name)
                put(PropertyAmenitiesTable.COLUMN_AMENITY_TYPE, it.type)
                put(PropertyAmenitiesTable.COLUMN_COUNT, it.count)
            }

            it.id = db.insert("t_property_amenities", null, values)
        }
    }
}