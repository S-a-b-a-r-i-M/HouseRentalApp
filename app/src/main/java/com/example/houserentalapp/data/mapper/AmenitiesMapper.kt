package com.example.houserentalapp.data.mapper

import com.example.houserentalapp.data.local.db.entity.PropertyAmenityEntity
import com.example.houserentalapp.domain.model.Amenity
import com.example.houserentalapp.domain.model.enums.AmenityType

object AmenitiesMapper {
    fun fromDomain(amenities: List<Amenity>): List<PropertyAmenityEntity> {
        return amenities.map {
            PropertyAmenityEntity(
                amenity = it.amenity,
                amenityType = it.amenityType.name,
                count = it.count
            )
        }
    }

    fun toDomain(entity: List<PropertyAmenityEntity>): List<Amenity> {
        return entity.map {
            if(it.id == null)
                throw IllegalArgumentException("Property Image id is missing")

            Amenity(
                id = it.id,
                amenity = it.amenity,
                amenityType = AmenityType.valueOf(it.amenityType),
                count = it.count
            )
        }
    }
}