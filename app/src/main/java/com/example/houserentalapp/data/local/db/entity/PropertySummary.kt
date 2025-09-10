package com.example.houserentalapp.data.local.db.entity

data class PropertySummaryEntity (
    val id: Long,
    val name: String,
    val description: String?,
    val lookingTo: String,
    val type: String,
    val furnishingType: String,
    val bhk: String,
    val price: Int,
    val builtUpArea: Int,
    val address: PropertyAddressEntity,
    val isActive: Boolean,
    val images: List<PropertyImageEntity>, // Need to take all images
    val createdAt: Long,
    val viewCount: Int = 0,
)
