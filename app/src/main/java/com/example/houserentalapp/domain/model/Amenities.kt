package com.example.houserentalapp.domain.model

data class Amenities (
    val socialAmenities: List<SocialAmenities>?,
    val internalAmenities: List<InternalAmenities>?,
    val countableInternalAmenities: List<CountableInternalAmenities>?
)

data class SocialAmenities(val amenityId: Long, val name: String)

data class InternalAmenities(val amenityId: Long, val name: String)

data class CountableInternalAmenities(val amenityId: Long, val name: String, val count: Int)
