package com.example.houserentalapp.data.local.db.entity

data class AmenitiesEntity(
    val socialAmenities: List<SocialAmenitiesEntity>?,
    val internalAmenities: List<InternalAmenitiesEntity>?,
    val countableInternalAmenities: List<CountableInternalAmenitiesEntity>?
)

data class SocialAmenitiesEntity(val amenityId: Long?, val name: String)

data class InternalAmenitiesEntity(val amenityId: Long?, val name: String)

data class CountableInternalAmenitiesEntity(val amenityId: Long?, val name: String, val count: Int)

