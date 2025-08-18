package com.example.houserentalapp.data.local.db.entity

data class PropertyAmenityEntity(
    var id: Long? = null,
    val amenity: String,
    val amenityType: String,
    val count: Int? = null
)