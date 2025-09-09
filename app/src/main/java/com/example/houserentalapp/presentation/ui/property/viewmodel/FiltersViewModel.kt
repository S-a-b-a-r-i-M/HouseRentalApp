package com.example.houserentalapp.presentation.ui.property.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.houserentalapp.domain.model.PropertyFilters
import com.example.houserentalapp.domain.model.enums.BHK
import com.example.houserentalapp.domain.model.enums.FurnishingType
import com.example.houserentalapp.domain.model.enums.PropertyType
import com.example.houserentalapp.domain.model.enums.TenantType
import com.example.houserentalapp.presentation.utils.extensions.logDebug

class FiltersViewModel : ViewModel() {
    private val _filtersUI = MutableLiveData(PropertyFilters())
    val filters: LiveData<PropertyFilters> = _filtersUI

    // This is an indication to trigger fetching data
    private val _applyFilters = MutableLiveData(false)
    val applyFilters: LiveData<Boolean> = _applyFilters

    fun triggerApplyFilters() {
        _applyFilters.value = true
        logDebug("Apply Filters Triggered.")
    }

    fun onFiltersApplied() {
        _applyFilters.value = false
        logDebug("Filters applied successfully.")
    }

    private inline fun updateFilter(update: (PropertyFilters) -> PropertyFilters) {
        val currentFilters = _filtersUI.value ?: PropertyFilters()
        _filtersUI.value = update(currentFilters)
    }

    fun setSearchQuery(newQuery: String) = updateFilter { it.copy(searchQuery = newQuery) }

    fun setBHKTypes(newData: List<BHK>) = updateFilter { it.copy(bhkTypes = newData) }

    fun setBudget(newData: Pair<Int, Int>) = updateFilter { it.copy(budget = newData) }

    fun setTenantTypes(newData: List<TenantType>) = updateFilter { it.copy(tenantTypes = newData) }

    fun setPropertyTypes(newData: List<PropertyType>) = updateFilter { it.copy(propertyTypes = newData) }

    fun setFurnishingTypes(newData: List<FurnishingType>) = updateFilter { it.copy(furnishingTypes = newData) }

    fun setOnlyShortlisted(newData: Boolean) = updateFilter { it.copy(onlyShortlisted = newData) }

    fun setOnlyAvailable(newData: Boolean) = updateFilter { it.copy(onlyAvailable = newData) }

    fun setLandlordId(newData: Long) = updateFilter { it.copy(landlordId = newData) }

    fun resetFilters() {
        _filtersUI.value = PropertyFilters()
        logDebug("Filters have reset.")
    }

    fun setPropertyFilters(filters: PropertyFilters) {
        _filtersUI.value = filters
        logDebug("filters have set. $filters")
    }
}