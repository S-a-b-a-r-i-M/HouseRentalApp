package com.example.houserentalapp.domain.model

import com.example.houserentalapp.domain.model.enums.BHK
import com.example.houserentalapp.domain.model.enums.FurnishingType
import com.example.houserentalapp.domain.model.enums.LookingTo
import com.example.houserentalapp.domain.model.enums.PropertyType

data class PropertySummary(
    val id: Long,
    val name: String,
    val description: String?,
    val lookingTo: LookingTo,
    val type: PropertyType,
    val furnishingType: FurnishingType,
    val bhk: BHK,
    val price: Int,
    val builtUpArea: Int,
    val address: PropertyAddress,
    val images: List<PropertyImage>,
    val isActive: Boolean,
    val createdAt: Long,
    val viewCount: Int = 0, // Used At your property
)
