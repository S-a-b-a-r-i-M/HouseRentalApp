package com.example.houserentalapp.domain.model

import com.example.houserentalapp.domain.model.enums.BHK
import com.example.houserentalapp.domain.model.enums.FurnishingType
import com.example.houserentalapp.domain.model.enums.PropertyType
import com.example.houserentalapp.domain.model.enums.TenantType

data class PropertyFilters (
    val searchQuery: String = "",
    val bhkTypes: List<BHK> = emptyList(),
    val propertyTypes: List<PropertyType> = emptyList(),
    val furnishingTypes: List<FurnishingType> = emptyList(),
    val tenantTypes: List<TenantType> = emptyList(),
    val budget: Pair<Int, Int>? = null,
    val onlyAvailable: Boolean = true,
    val onlyShortlisted: Boolean = false, // Only shortlisted properties
    val onlyUserProperties: Boolean = false, // Only his uploaded properties
)
