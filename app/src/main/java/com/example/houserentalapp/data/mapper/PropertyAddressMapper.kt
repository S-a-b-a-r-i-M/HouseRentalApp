package com.example.houserentalapp.data.mapper

import com.example.houserentalapp.data.local.db.entity.PropertyAddressEntity
import com.example.houserentalapp.domain.model.PropertyAddress

object PropertyAddressMapper {
    fun fromDomain(address: PropertyAddress): PropertyAddressEntity {
        return PropertyAddressEntity(
            streetName = address.streetName,
            locality = address.locality,
            city = address.city
        )
    }

    fun toDomain(entity: PropertyAddressEntity): PropertyAddress {
        return PropertyAddress(
            streetName = entity.streetName,
            locality = entity.locality,
            city = entity.city
        )
    }
}