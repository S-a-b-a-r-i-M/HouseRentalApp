package com.example.houserentalapp.domain.model

import com.example.houserentalapp.domain.model.enums.BHK
import com.example.houserentalapp.domain.model.enums.FurnishingType
import com.example.houserentalapp.domain.model.enums.LookingTo
import com.example.houserentalapp.domain.model.enums.TenantType
import com.example.houserentalapp.domain.model.enums.PropertyKind
import com.example.houserentalapp.domain.model.enums.PropertyTransactionType
import com.example.houserentalapp.domain.model.enums.PropertyType

data class Property (
    val id: Long,
    val landlordId: Long,
    val name: String,
    val description: String?,
    val lookingTo: LookingTo,
    val kind: PropertyKind,
    val type: PropertyType,
    val furnishingType: FurnishingType,
    val amenities: Amenities,
    val preferredTenantType: TenantType,
    val transactionType: PropertyTransactionType, // Specific to Sell
    val ageOfProperty: Int?,
    val countOfCoveredParking: Int,
    val availableFrom: Int, // EPOCH
    val bhk: BHK,
    // val bathRoomCount: Int, // optional
    val isPetAllowed: Boolean,
    val isAvailable: Boolean,
    val viewCount: Int = 0,
    // Address
    val address: PropertyAddress,
    // Budget Related
    val price: Float,
    val isMaintenanceSeparate: Boolean,
    val maintenanceCharges: Float?,
    val numberOfSecurityDepositMonths: Int,
    val numberOfLockInMonths: Int,
    // TimeLine
    val createdAt: Int
    ) {
    init {
        // Validation
        // Name
        require(name.trim().length in 3..50) {
            "Property name must be between 3 and 50 characters"
        }

        // Furnishing type validation
        when (furnishingType) {
            FurnishingType.SEMI_FURNISHED -> {
                val internalCount = amenities.internalAmenities?.size ?: 0
                val countableCount = amenities.countableInternalAmenities?.size ?: 0
                require(internalCount + countableCount >= 3) {
                    "Semi-furnished property must have at least 3 internal amenities"
                }
            }
            FurnishingType.FULLY_FURNISHED -> {
                val internalCount = amenities.internalAmenities?.size ?: 0
                val countableCount = amenities.countableInternalAmenities?.size ?: 0
                require(internalCount + countableCount >= 5) {
                    "Fully-furnished property must have at least 5 internal amenities"
                }
            }
            FurnishingType.UN_FURNISHED -> {
                // No specific requirement for unfurnished
            }
        }

        // TransactionType
        if (transactionType == PropertyTransactionType.RESALE)
            requireNotNull(ageOfProperty) {
                "Age of property is required for resale properties"
            }

        // Maintenance charges
        if (isMaintenanceSeparate) {
            requireNotNull(maintenanceCharges) {
                "Maintenance charges must be specified when maintenance is separate"
            }
            require(maintenanceCharges >= 0) {
                "Maintenance charges cannot be negative"
            }
        }

    }
}

data class PropertyAddress (
    val streetName: String,
    val locality: String,
    val city: String,
)

data class Amenities (
    val socialAmenities: List<SocialAmenities>?,
    val internalAmenities: List<InternalAmenities>?,
    val countableInternalAmenities: List<CountableInternalAmenities>?
)

data class SocialAmenities(val amenityId: Long, val name: String)

data class InternalAmenities(val amenityId: Long, val name: String)

data class CountableInternalAmenities(val amenityId: Long, val name: String, val count: Int)