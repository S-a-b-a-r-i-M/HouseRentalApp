package com.example.houserentalapp.data.mapper

import android.database.Cursor
import com.example.houserentalapp.data.local.db.entity.PropertyAddressEntity
import com.example.houserentalapp.data.local.db.entity.PropertyEntity
import com.example.houserentalapp.data.local.db.entity.PropertySummaryEntity
import com.example.houserentalapp.data.local.db.tables.PropertyTable
import com.example.houserentalapp.domain.model.Property
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.domain.model.enums.BHK
import com.example.houserentalapp.domain.model.enums.BachelorType
import com.example.houserentalapp.domain.model.enums.FurnishingType
import com.example.houserentalapp.domain.model.enums.LookingTo
import com.example.houserentalapp.domain.model.enums.PropertyKind
import com.example.houserentalapp.domain.model.enums.PropertyTransactionType
import com.example.houserentalapp.domain.model.enums.PropertyType
import com.example.houserentalapp.domain.model.enums.TenantType

object PropertyMapper {
    fun fromDomain(domain: Property) : PropertyEntity {
        return PropertyEntity(
            id = domain.id,
            landlordId = domain.landlordId,
            name = domain.name,
            description = domain.description,
            lookingTo = domain.lookingTo.readable,
            kind = domain.kind.readable,
            type = domain.type.readable,
            furnishingType = domain.furnishingType.readable,
            amenities = AmenitiesMapper.fromDomain(domain.amenities),
            preferredTenants = domain.preferredTenantType.joinToString(",") { it.readable },
            preferredBachelorType = domain.preferredBachelorType?.readable,
            transactionType = domain.transactionType?.readable,
            ageOfProperty = domain.ageOfProperty,
            countOfCoveredParking = domain.countOfCoveredParking,
            countOfOpenParking = domain.countOfOpenParking,
            availableFrom = domain.availableFrom,
            bhk = domain.bhk.readable,
            builtUpArea = domain.builtUpArea,
            bathRoomCount = domain.bathRoomCount,
            isPetAllowed = domain.isPetAllowed,
            isAvailable = domain.isAvailable,
            viewCount = domain.viewCount,
            price = domain.price,
            isMaintenanceSeparate = domain.isMaintenanceSeparate,
            maintenanceCharges = domain.maintenanceCharges,
            securityDepositAmount = domain.securityDepositAmount,
            address = PropertyAddressMapper.fromDomain(domain.address),
            images = emptyList(), // No images here at creation
            createdAt = domain.createdAt
        )
    }

    fun toDomain(entity: PropertyEntity): Property {
        return Property(
            id = entity.id,
            landlordId = entity.landlordId,
            name = entity.name,
            description = entity.description,
            lookingTo = LookingTo.fromString(entity.lookingTo),
            kind = PropertyKind.fromString(entity.kind),
            type = PropertyType.fromString(entity.type),
            furnishingType = FurnishingType.fromString(entity.furnishingType),
            amenities = AmenitiesMapper.toDomain(entity.amenities),
            preferredTenantType = entity.preferredTenants
                .split(",")
                .map { TenantType.fromString(it) },
            preferredBachelorType = entity.preferredBachelorType?.let { BachelorType.fromString(it) },
            transactionType = entity.transactionType?.let { PropertyTransactionType.fromString(it) },
            ageOfProperty = entity.ageOfProperty,
            countOfCoveredParking = entity.countOfCoveredParking,
            countOfOpenParking = entity.countOfOpenParking,
            availableFrom = entity.availableFrom,
            bhk = BHK.fromString(entity.bhk),
            builtUpArea = entity.builtUpArea,
            bathRoomCount = entity.bathRoomCount,
            isPetAllowed = entity.isPetAllowed,
            isAvailable = entity.isAvailable,
            viewCount = entity.viewCount,
            price = entity.price,
            isMaintenanceSeparate = entity.isMaintenanceSeparate,
            maintenanceCharges = entity.maintenanceCharges,
            securityDepositAmount = entity.securityDepositAmount,
            address = PropertyAddressMapper.toDomain(entity.address),
            images = entity.images.map { PropertyImageMapper.toDomain(it) },
            createdAt = entity.createdAt
        )
    }

    fun toPropertySummaryDomain(entity: PropertySummaryEntity): PropertySummary {
        return PropertySummary(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            lookingTo = LookingTo.fromString(entity.lookingTo),
            type = PropertyType.fromString(entity.type),
            furnishingType = FurnishingType.fromString(entity.furnishingType),
            bhk = BHK.fromString(entity.bhk),
            price = entity.price,
            builtUpArea = entity.builtUpArea,
            address = PropertyAddressMapper.toDomain(entity.address),
            images = entity.images.map { PropertyImageMapper.toDomain(it) },
            viewCount = entity.viewCount
        )
    }

    fun toPropertySummaryEntity(cursor: Cursor): PropertySummaryEntity {
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
                images = emptyList(), // Will be populated separately
            )
        }
    }
}