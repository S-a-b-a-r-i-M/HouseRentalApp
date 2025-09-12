package com.example.houserentalapp.presentation.model

import com.example.houserentalapp.domain.model.enums.BHK
import com.example.houserentalapp.domain.model.enums.BachelorType
import com.example.houserentalapp.domain.model.enums.FurnishingType
import com.example.houserentalapp.domain.model.enums.LookingTo
import com.example.houserentalapp.domain.model.enums.PropertyKind
import com.example.houserentalapp.domain.model.enums.PropertyType
import com.example.houserentalapp.domain.model.enums.TenantType

data class PropertyBasicUI(
    val name: String = "",
    val description: String = "",
    val lookingTo: LookingTo = LookingTo.RENT,
    val kind: PropertyKind? = PropertyKind.RESIDENTIAL,
    val type: PropertyType? = null,
    val bhk: BHK? = null,
    val builtUpArea: String = "",
    val bathRoomCount: String = "0",
)

data class PropertyPreferencesUI(
    val furnishingType: FurnishingType? = null,
    val preferredTenantTypes: List<TenantType>? = null,
    val preferredBachelorType: BachelorType? = null,
    val isPetAllowed: Boolean? = null,
    val countOfCoveredParking: String = "0",
    val countOfOpenParking: String = "0",
    val availableFrom: String = "",
)

data class PropertyPricingUI(
    val price: String = "",
    val isMaintenanceSeparate: Boolean? = null,
    val maintenanceCharges: String = "",
    val securityDepositAmount: String = ""
)

data class PropertyAddressUI(
    val street: String = "",
    val locality: String = "",
    val city: String = "",
)