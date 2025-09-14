package com.example.houserentalapp.data.mapper

import com.example.houserentalapp.data.local.db.entity.AmenityEntity
import com.example.houserentalapp.domain.model.AmenityDomain
import com.example.houserentalapp.domain.model.enums.AmenityBaseEnum
import com.example.houserentalapp.domain.model.enums.AmenityType
import com.example.houserentalapp.domain.model.enums.CountableInternalAmenity
import com.example.houserentalapp.domain.model.enums.InternalAmenity
import com.example.houserentalapp.domain.model.enums.SocialAmenity
import com.example.houserentalapp.presentation.utils.extensions.logError

object AmenitiesMapper {
    fun fromDomain(amenities: List<AmenityDomain>): List<AmenityEntity> {
        return amenities.map {
            AmenityEntity(
                name = it.name.readable,
                type = it.type.readable,
                count = it.count
            )
        }
    }

    fun toDomain(entity: List<AmenityEntity>): List<AmenityDomain> {
        return entity.mapNotNull {
            try {
                if(it.id == 0L)
                    throw IllegalArgumentException("Amenity id is required for domain model.")

                val amenityType = AmenityType.fromString(it.type)
                val amenityName: AmenityBaseEnum = when(amenityType) {
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