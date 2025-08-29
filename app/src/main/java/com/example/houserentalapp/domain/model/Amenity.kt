package com.example.houserentalapp.domain.model

import com.example.houserentalapp.domain.model.enums.AmenityType

data class Amenity (
    val id: Long?,
    val name: String,
    val type: AmenityType,
    val count: Int? = null
)
