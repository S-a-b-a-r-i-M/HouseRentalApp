package com.example.houserentalapp.domain.model

import com.example.houserentalapp.domain.model.enums.AmenityEnum
import com.example.houserentalapp.domain.model.enums.AmenityType

data class AmenityDomain (
    val id: Long?,
    val name: AmenityEnum,
    val type: AmenityType,
    val count: Int? = null
)
