package com.example.houserentalapp.presentation.enums

import com.example.houserentalapp.domain.model.enums.BHK
import com.example.houserentalapp.domain.model.enums.BachelorType
import com.example.houserentalapp.domain.model.enums.FurnishingType
import com.example.houserentalapp.domain.model.enums.LookingTo
import com.example.houserentalapp.domain.model.enums.PropertyKind
import com.example.houserentalapp.domain.model.enums.PropertyType
import com.example.houserentalapp.domain.model.enums.TenantType

enum class PropertyFormField {
    NAME,
    DESCRIPTION,
    LOOKING_TO,
    KIND,
    TYPE,
    FURNISHING_TYPE,
    PREFERRED_TENANT_TYPE,
    PREFERRED_BACHELOR_TYPE,
//    TRANSACTION_TYPE,
//    AGE_OF_PROPERTY,
    COVERED_PARKING_COUNT,
    OPEN_PARKING_COUNT,
    AVAILABLE_FROM,
    BHK,
    BUILT_UP_AREA,
    BATH_ROOM_COUNT,
    IS_PET_FRIENDLY,
    PRICE,
    IS_MAINTENANCE_SEPARATE,
    MAINTENANCE_CHARGES,
    SECURITY_DEPOSIT,
    STREET,
    LOCALITY,
    CITY
}

data class PropertyBasicUI(
    val name: String? = null,
    val description: String? = null,
    val lookingTo: LookingTo? = null,
    val kind: PropertyKind? = PropertyKind.RESIDENTIAL,
    val type: PropertyType? = null,
    val bhk: BHK? = null,
    val builtUpArea: String? = null,
    val bathRoomCount: String? = null,
)

data class PropertyPreferencesUI(
    val furnishingType: FurnishingType? = null,
    val preferredTenantTypes: List<TenantType>? = null,
    val preferredBachelorType: BachelorType? = null,
    val isPetAllowed: Boolean? = null,
    val countOfCoveredParking: String? = null,
    val countOfOpenParking: String? = null,
    val availableFrom: String? = null,
)

data class PropertyPricingUI(
    val price: String? = null,
    val isMaintenanceSeparate: Boolean? = null,
    val maintenanceCharges: String? = null,
    val securityDepositAmount: String? = null
)

data class PropertyAddressUI(
    val street: String? = null,
    val locality: String? = null,
    val city: String? = null,
)