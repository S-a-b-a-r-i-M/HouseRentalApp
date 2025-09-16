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
    val budget: Pair<Float, Float>? = null,
    val onlyShortlisted: Boolean = false, // Only shortlisted properties
    // Internal Filters
    val onlyAvailable: Boolean = true,
    val onlyUserProperties: Boolean = false, // Only his uploaded properties
)

fun PropertyFilters.getAddedFiltersCount() : Int {
    var count = 0

    if (searchQuery.isNotEmpty()) count++

    if (bhkTypes.isNotEmpty()) count++

    if (furnishingTypes.isNotEmpty()) count++

    if (propertyTypes.isNotEmpty()) count++

    if (tenantTypes.isNotEmpty()) count++

    if (budget != null) count++

    if (onlyShortlisted) count++

    return count
}

