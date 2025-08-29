package com.example.houserentalapp.data.mapper

import com.example.houserentalapp.data.local.db.entity.PropertyAmenityEntity
import com.example.houserentalapp.domain.model.Amenity
import com.example.houserentalapp.domain.model.enums.AmenityType

object AmenitiesMapper {
    fun fromDomain(amenities: List<Amenity>): List<PropertyAmenityEntity> {
        return amenities.map {
            PropertyAmenityEntity(
                amenity = it.name,
                amenityType = it.type.name,
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
                name = it.amenity,
                type = AmenityType.valueOf(it.amenityType),
                count = it.count
            )
        }
    }
}