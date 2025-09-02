package com.example.houserentalapp.presentation.ui.property.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.houserentalapp.domain.model.Pagination
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.model.enums.UserActionEnum
import com.example.houserentalapp.domain.usecase.GetPropertyUseCase
import com.example.houserentalapp.domain.usecase.TenantRelatedPropertyUseCase
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logError
import kotlinx.coroutines.launch
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.enums.PropertyFiltersField
import com.example.houserentalapp.presentation.utils.extensions.logWarning

class PropertiesListViewModel(
    private val getPropertyUC: GetPropertyUseCase,
    private val tenantRelatedPropertyUC: TenantRelatedPropertyUseCase,
    private val currentUser: User
) : ViewModel() {
    private val _propertySummariesResult = MutableLiveData<ResultUI<List<PropertySummary>>>()
    val propertySummariesResult: LiveData<ResultUI<List<PropertySummary>>> = _propertySummariesResult
    private val propertySummaryList: MutableList<PropertySummary> = mutableListOf() // Used to hold Property Summary Data
    private val _filters = MutableLiveData<Map<PropertyFiltersField, Any>>()
    val filters: LiveData<Map<PropertyFiltersField, Any>> = _filters
    private var hasMore: Boolean = true
    private var offset: Int = 0
    private val limit: Int = 10

    fun loadPropertySummaries(onlyShortlistedRecords: Boolean) {
        if (!hasMore) return
        _propertySummariesResult.value = ResultUI.Loading
        viewModelScope.launch {
            try {
                val pagination = Pagination(offset, limit)
                val result = if (onlyShortlistedRecords) // Get Only Shortlisted Properties
                    tenantRelatedPropertyUC.getPropertySummariesByUserAction(
                        currentUser.id,
                        UserActionEnum.SHORTLISTED,
                        pagination
                    )
                else // Get Properties With Filters
                    getPropertyUC.getPropertySummaries(getStringFilters(), pagination)

                when (result) {
                    is Result.Success<List<PropertySummary>> -> {
                        propertySummaryList.addAll(result.data)
                        _propertySummariesResult.value = ResultUI.Success(propertySummaryList)
                        // Does it has more ?
                        val totalRecords = result.meta?.get("total_records")
                        if (totalRecords == null) {
                            logWarning("loadPropertySummaries -> total record count is missing")
                            hasMore = result.data.size == limit
                        } else
                            hasMore = propertySummaryList.size < (totalRecords as Int)
                        // Update offset
                        offset += limit
                    }
                    is Result.Error -> {
                        logError("Error on loadPropertySummaries : ${result.message}")
                        _propertySummariesResult.value = ResultUI.Error(result.message)
                    }
                }
            } catch (exp: Exception) {
                logError("Error on loadPropertySummaries : ${exp.message}")
                _propertySummariesResult.value = ResultUI.Error("Unexpected Error")
            }
        }
    }

    private fun getStringFilters() = _filters.value?.mapKeys { it.key.name } ?: mutableMapOf()

    // Will get triggered by filters modification
    fun refresh() {
        offset = 0
        hasMore = true
        propertySummaryList.clear()
        _propertySummariesResult.value = ResultUI.Success(emptyList())
    }

    fun hasMore() = hasMore
}


class PropertiesListViewModelFactory(
    private val getPropertyUC: GetPropertyUseCase,
    private val tenantRelatedPropertyUC: TenantRelatedPropertyUseCase,
    private val currentUser: User
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PropertiesListViewModel::class.java))
            return PropertiesListViewModel(getPropertyUC, tenantRelatedPropertyUC, currentUser) as T

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}