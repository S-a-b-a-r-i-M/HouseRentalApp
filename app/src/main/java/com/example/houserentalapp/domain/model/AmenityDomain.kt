package com.example.houserentalapp.domain.model

import com.example.houserentalapp.domain.model.enums.AmenityBaseEnum
import com.example.houserentalapp.domain.model.enums.AmenityType

data class AmenityDomain (
    val id: Long,
    val name: AmenityBaseEnum,
    val type: AmenityType,
    val count: Int? = null
)
