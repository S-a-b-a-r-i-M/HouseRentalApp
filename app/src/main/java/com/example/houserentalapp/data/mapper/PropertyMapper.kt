package com.example.houserentalapp.data.mapper

import com.example.houserentalapp.data.local.db.entity.PropertyEntity
import com.example.houserentalapp.data.local.db.entity.PropertySummaryEntity
import com.example.houserentalapp.domain.model.Property
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.domain.model.enums.BHK
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
            lookingTo = domain.lookingTo.name,
            kind = domain.kind.name,
            type = domain.type.name,
            furnishingType = domain.furnishingType.name,
            amenities = AmenitiesMapper.fromDomain(domain.amenities),
            preferredTenants = domain.preferredTenantType.joinToString(","),
            preferredBachelorType = domain.preferredBachelorType,
            transactionType = domain.transactionType?.name,
            ageOfProperty = domain.ageOfProperty,
            countOfCoveredParking = domain.countOfCoveredParking,
            countOfOpenParking = domain.countOfOpenParking,
            availableFrom = domain.availableFrom,
            bhk = domain.bhk.name,
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
            lookingTo = LookingTo.valueOf(entity.lookingTo),
            kind = PropertyKind.valueOf(entity.kind),
            type = PropertyType.valueOf(entity.type),
            furnishingType = FurnishingType.valueOf(entity.furnishingType),
            amenities = AmenitiesMapper.toDomain(entity.amenities),
            preferredTenantType = entity.preferredTenants
                .split(",")
                .map { TenantType.fromString(it)!! },
            preferredBachelorType = entity.preferredBachelorType,
            transactionType = entity.transactionType?.let { PropertyTransactionType.valueOf(it) },
            ageOfProperty = entity.ageOfProperty,
            countOfCoveredParking = entity.countOfCoveredParking,
            countOfOpenParking = entity.countOfOpenParking,
            availableFrom = entity.availableFrom,
            bhk = BHK.valueOf(entity.bhk),
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

    fun toPropertySummary(entity: PropertySummaryEntity): PropertySummary {
        return PropertySummary(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            lookingTo = LookingTo.valueOf(entity.lookingTo),
            type = PropertyType.valueOf(entity.type),
            furnishingType = FurnishingType.valueOf(entity.furnishingType),
            bhk = BHK.valueOf(entity.bhk),
            price = entity.price,
            builtUpArea = entity.builtUpArea,
            address = PropertyAddressMapper.toDomain(entity.address),
            images = entity.images.map { PropertyImageMapper.toDomain(it) },
            viewCount = entity.viewCount
        )
    }
}