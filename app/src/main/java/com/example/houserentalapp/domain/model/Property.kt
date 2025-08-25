package com.example.houserentalapp.domain.model

import com.example.houserentalapp.domain.model.enums.BHK
import com.example.houserentalapp.domain.model.enums.BachelorType
import com.example.houserentalapp.domain.model.enums.FurnishingType
import com.example.houserentalapp.domain.model.enums.LookingTo
import com.example.houserentalapp.domain.model.enums.TenantType
import com.example.houserentalapp.domain.model.enums.PropertyKind
import com.example.houserentalapp.domain.model.enums.PropertyTransactionType
import com.example.houserentalapp.domain.model.enums.PropertyType

data class  Property (
    val id: Long?,
    val landlordId: Long,
    val name: String,
    val description: String?,
    val lookingTo: LookingTo,
    val kind: PropertyKind,
    val type: PropertyType,
    val furnishingType: FurnishingType,
    val amenities: List<Amenity>, // On-hold
    val preferredTenantType: List<TenantType>,
    val preferredBachelorType: String? = BachelorType.BOTH.readable,
    val transactionType: PropertyTransactionType?, // Specific to Sell
    val ageOfProperty: Int?, // Specific to Sell
    val countOfCoveredParking: Int,
    val countOfOpenParking: Int,
    val availableFrom: Long,
    val bhk: BHK,
    val builtUpArea: Int,
    val bathRoomCount: Int = 0, // optional
    val isPetAllowed: Boolean,
    val isAvailable: Boolean,
    val viewCount: Int = 0,
    // Budget Related
    val price: Int,
    val isMaintenanceSeparate: Boolean,
    val maintenanceCharges: Int?,
    val securityDepositAmount: Int?,
    // Address
    val address: PropertyAddress,
    // Images
    val images: List<PropertyImage>,
    // TimeLine
    val createdAt: Long = System.currentTimeMillis()
    ) {
    init {
        // Domain Level Validation
    }
}