package com.example.houserentalapp.data.mapper

import com.example.houserentalapp.data.local.db.entity.PropertyAmenityEntity
import com.example.houserentalapp.domain.model.AmenityDomain
import com.example.houserentalapp.domain.model.enums.AmenityEnum
import com.example.houserentalapp.domain.model.enums.AmenityType
import com.example.houserentalapp.domain.model.enums.CountableInternalAmenity
import com.example.houserentalapp.domain.model.enums.InternalAmenity
import com.example.houserentalapp.domain.model.enums.SocialAmenity
import com.example.houserentalapp.presentation.utils.extensions.logError

object AmenitiesMapper {
    fun fromDomain(amenities: List<AmenityDomain>): List<PropertyAmenityEntity> {
        return amenities.map {
            PropertyAmenityEntity(
                name = it.name.readable,
                type = it.type.name,
                count = it.count
            )
        }
    }

    fun toDomain(entity: List<PropertyAmenityEntity>): List<AmenityDomain> {
        return entity.mapNotNull {
            try {
                if(it.id == null)
                    throw IllegalArgumentException("Amenity id is required for domain model.")

                val amenityType = AmenityType.valueOf(it.type)
                val amenityName: AmenityEnum = when(amenityType) {
                    AmenityType.INTERNAL -> InternalAmenity.fromString(it.name)
                    AmenityType.INTERNAL_COUNTABLE -> CountableInternalAmenity.fromString(it.name)
                    AmenityType.SOCIAL -> SocialAmenity.fromString(it.name)
                } ?: throw IllegalArgumentException("Invalid Amenity Name ${it.name}")

                AmenityDomain(
                    id = it.id,
                    name = amenityName,
                    type = amenityType,
                    count = it.count
                )
            } catch (exp: Exception) {
                logError("Failed to parse amenity: ${it.name}, error: ${exp.message}")
                null
            }
        }
    }
}