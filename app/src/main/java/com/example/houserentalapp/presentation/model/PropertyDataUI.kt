package com.example.houserentalapp.presentation.model

import com.example.houserentalapp.domain.model.enums.BHK
import com.example.houserentalapp.domain.model.enums.BachelorType
import com.example.houserentalapp.domain.model.enums.FurnishingType
import com.example.houserentalapp.domain.model.enums.LookingTo
import com.example.houserentalapp.domain.model.enums.PropertyKind
import com.example.houserentalapp.domain.model.enums.PropertyType
import com.example.houserentalapp.domain.model.enums.TenantType

data class PropertyDataUI(
    var name: String = "",
    var description: String = "",
    var lookingTo: LookingTo = LookingTo.RENT,
    var kind: PropertyKind? = PropertyKind.RESIDENTIAL,
    var type: PropertyType? = null,
    var bhk: BHK? = null,
    var builtUpArea: String = "",
    var bathRoomCount: String = "0",
    var price: String = "",
    var isMaintenanceSeparate: Boolean? = null,
    var maintenanceCharges: String = "",
    var securityDepositAmount: String = "",
    var furnishingType: FurnishingType? = null,
    var preferredTenantTypes: List<TenantType>? = null,
    var preferredBachelorType: BachelorType? = null,
    var isPetAllowed: Boolean? = null,
    var countOfCoveredParking: String = "0",
    var countOfOpenParking: String = "0",
    var availableFrom: String = "",
    var street: String = "",
    var locality: String = "",
    var city: String = "",
)