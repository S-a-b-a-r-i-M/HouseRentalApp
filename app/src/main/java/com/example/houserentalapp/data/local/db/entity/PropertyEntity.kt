package com.example.houserentalapp.data.local.db.entity

data class PropertyEntity (
    val id: Long?,
    val landlordId: Long,
    val name: String,
    val description: String?,
    val lookingTo: String,
    val kind: String,
    val type: String,
    val furnishingType: String,
    val amenities: List<PropertyAmenityEntity>,
    val preferredTenants: String,
    val preferredBachelorType: String?,
    val transactionType: String?,
    val ageOfProperty: Int?,
    val countOfCoveredParking: Int,
    val countOfOpenParking: Int,
    val availableFrom: Long, // milli seconds
    val bhk: String,
    val builtUpArea: Int,
    val bathRoomCount: Int = 0,
    val isPetAllowed: Boolean,
    val isActive: Boolean,
    val viewCount: Int = 0,
    // Budget Related
    val price: Int,
    val isMaintenanceSeparate: Boolean,
    val maintenanceCharges: Int?,
    val securityDepositAmount: Int?,
    // Address
    val address: PropertyAddressEntity,
    // Images
    val images: List<PropertyImageEntity>,
    // TimeLine
    val createdAt: Long
)
