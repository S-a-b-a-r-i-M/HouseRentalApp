package com.example.houserentalapp.data.mapper

import com.example.houserentalapp.data.local.db.entity.AmenitiesEntity
import com.example.houserentalapp.data.local.db.entity.CountableInternalAmenitiesEntity
import com.example.houserentalapp.data.local.db.entity.InternalAmenitiesEntity
import com.example.houserentalapp.data.local.db.entity.SocialAmenitiesEntity
import com.example.houserentalapp.domain.model.Amenities
import com.example.houserentalapp.domain.model.CountableInternalAmenities
import com.example.houserentalapp.domain.model.InternalAmenities
import com.example.houserentalapp.domain.model.SocialAmenities

object AmenitiesMapper {
    fun fromDomain(amenities: Amenities): AmenitiesEntity {
        return AmenitiesEntity(
            socialAmenities = amenities.socialAmenities?.map {
                SocialAmenitiesEntity(it.amenityId, it.name)
            },
            internalAmenities = amenities.internalAmenities?.map {
                InternalAmenitiesEntity(it.amenityId, it.name)
            },
            countableInternalAmenities = amenities.countableInternalAmenities?.map {
                CountableInternalAmenitiesEntity(it.amenityId, it.name, it.count)
            }
        )
    }

    fun toDomain(entity: AmenitiesEntity): Amenities {
        return Amenities(
            socialAmenities = entity.socialAmenities?.map {
                if(it.amenityId == null)
                    throw IllegalArgumentException("socialAmenity id is missing for ${it.name}")

                SocialAmenities(it.amenityId, it.name)
            },
            internalAmenities = entity.internalAmenities?.map {
                if(it.amenityId == null)
                    throw IllegalArgumentException("internalAmenity id is missing for ${it.name}")

                InternalAmenities(it.amenityId, it.name)
            },
            countableInternalAmenities = entity.countableInternalAmenities?.map {
                if(it.amenityId == null)
                    throw IllegalArgumentException(
                        "countable internalAmenity id is missing for ${it.name}"
                    )

                CountableInternalAmenities(it.amenityId, it.name, it.count)
            }
        )
    }
}