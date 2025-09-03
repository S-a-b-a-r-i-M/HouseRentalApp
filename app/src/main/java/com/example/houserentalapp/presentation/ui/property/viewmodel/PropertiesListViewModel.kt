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
import com.example.houserentalapp.presentation.model.PropertySummaryUI
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.logWarning

class PropertiesListViewModel(
    private val getPropertyUC: GetPropertyUseCase,
    private val propertyUserActionUC: TenantRelatedPropertyUseCase,
    private val currentUser: User
) : ViewModel() {
    private val _propertySummariesResult = MutableLiveData<ResultUI<List<PropertySummaryUI>>>()
    val propertySummariesResult: LiveData<ResultUI<List<PropertySummaryUI>>> = _propertySummariesResult
    private val propertySummaryUIList: MutableList<PropertySummaryUI> = mutableListOf() // Used to hold Property Summary Data
    private val _filters = MutableLiveData<Map<PropertyFiltersField, Any>>(mutableMapOf())
    val filters: LiveData<Map<PropertyFiltersField, Any>> = _filters
    private var hasMore: Boolean = true
    private var offset: Int = 0
    private val limit: Int = 10

    fun addFilter(field: PropertyFiltersField, value: Any) {
        val updatedFilters = _filters.value!!.toMutableMap().apply {
            put(field, value)
        }
        _filters.value = updatedFilters
    }

    fun removeFilter(field: PropertyFiltersField) {
        val updatedFilters = _filters.value!!.toMutableMap().apply { remove(field) }
        _filters.value = updatedFilters
    }

    fun resetFilter(field: PropertyFiltersField) {
        _filters.value = mutableMapOf()
    }

    fun loadPropertySummaries(onlyShortlistedRecords: Boolean) {
        if (!hasMore) return
        _propertySummariesResult.value = ResultUI.Loading
        viewModelScope.launch {
            try {
                if (onlyShortlistedRecords)
                    addFilter(PropertyFiltersField.ONLY_SHORTLISTS, true)
                else
                    removeFilter(PropertyFiltersField.ONLY_SHORTLISTS)

                val result = getPropertyUC.getPropertySummaries(
                    currentUser.id,
                    getStringFilters(),
                    Pagination(offset, limit)
                )

                when (result) {
                    is Result.Success<List<Pair<PropertySummary, Boolean>>> -> {
                        propertySummaryUIList.addAll(
                            result.data.map {
                                PropertySummaryUI(it.first, it.second)
                            }
                        )
                        _propertySummariesResult.value = ResultUI.Success(propertySummaryUIList)

                        // Update Offset & HasMore
                        offset += limit
                        hasMore = result.data.size == limit
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

    fun togglePropertyShortlist(propertyId: Long, onSuccess: (Boolean) -> Unit, onFailure: () -> Unit) {
        val propertyIdx = propertySummaryUIList.indexOfFirst { it.summary.id == propertyId }
        if (propertyIdx == -1)  {
            logWarning("property $propertyId is not found")
            return
        }

        viewModelScope.launch {
            val summaryUI = propertySummaryUIList[propertyIdx]
            val result = if (summaryUI.isShortListed)
                propertyUserActionUC.deleteUserAction(
                    currentUser.id, propertyId, UserActionEnum.SHORTLISTED
                )
            else
                propertyUserActionUC.storeUserAction(
                    currentUser.id, propertyId, UserActionEnum.SHORTLISTED
                )

            when (result) {
                is Result.Success<*> -> {
                    logInfo("property shortlisted toggled")
                    val newState = !summaryUI.isShortListed
                    propertySummaryUIList[propertyIdx] = summaryUI.copy(isShortListed = newState)
                    _propertySummariesResult.value = ResultUI.Success(propertySummaryUIList)
                    onSuccess(newState)
                }

                is Result.Error -> {
                    logError("Error while toggling property to shortlisted")
                    onFailure()
                }
            }
        }
    }

    // Will get triggered by filters modification
    fun refresh() {
        offset = 0
        hasMore = true
        propertySummaryUIList.clear()
        _propertySummariesResult.value = ResultUI.Success(emptyList())
    }

    fun hasMore() = hasMore
}

class PropertiesListViewModelFactory(
    private val getPropertyUC: GetPropertyUseCase,
    private val propertyUserActionUC: TenantRelatedPropertyUseCase,
    private val currentUser: User
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PropertiesListViewModel::class.java))
            return PropertiesListViewModel(
                getPropertyUC,
                propertyUserActionUC,
                currentUser
            ) as T

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}