package com.example.houserentalapp.presentation.ui.property.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.houserentalapp.domain.model.Pagination
import com.example.houserentalapp.domain.model.PropertyFilters
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.model.enums.UserActionEnum
import com.example.houserentalapp.domain.usecase.PropertyUseCase
import com.example.houserentalapp.domain.usecase.SearchHistoryUseCase
import com.example.houserentalapp.domain.usecase.TenantRelatedPropertyUseCase
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logError
import kotlinx.coroutines.launch
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.model.PropertySummaryUI
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.logWarning

class PropertiesListViewModel(
    private val propertyUC: PropertyUseCase,
    private val propertyUserActionUC: TenantRelatedPropertyUseCase,
    private val searchHistoryUC: SearchHistoryUseCase,
    private val currentUser: User
) : ViewModel() {
    private val _propertySummariesResult = MutableLiveData<ResultUI<List<PropertySummaryUI>>>()
    val propertySummariesResult: LiveData<ResultUI<List<PropertySummaryUI>>> = _propertySummariesResult
    private val propertySummaryUIList: MutableList<PropertySummaryUI> = mutableListOf() // Used to hold Property Summary Data

    var hasMore: Boolean = true
        private set
    private var offset: Int = 0
    private val limit: Int = 10
    private var recentFilters: PropertyFilters? = null

    fun loadPropertySummaries(filters: PropertyFilters?) {
        val isFiltersChanged = recentFilters != filters
        if (isFiltersChanged) {
            // Reset data sets on filter change
            reset()
            recentFilters = filters
        }

        if (!hasMore) return
        _propertySummariesResult.value = ResultUI.Loading
        viewModelScope.launch {
            if (isFiltersChanged && filters != null && filters.searchQuery.trim().isNotEmpty())
                searchHistoryUC.storeSearchHistory(currentUser.id, filters)

            try {
                val result = propertyUC.getPropertySummaries(
                    currentUser.id,
                    Pagination(offset, limit),
                    filters
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

    fun togglePropertyShortlist(propertyId: Long, onSuccess: (Boolean) -> Unit, onFailure: () -> Unit) {
        val propertyIdx = propertySummaryUIList.indexOfFirst { it.summary.id == propertyId }
        if (propertyIdx == -1)  {
            logWarning("property $propertyId is not found")
            onFailure()
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
                    logInfo("property shortlist state toggled")
                    val newState = !summaryUI.isShortListed
                    if (!newState && recentFilters?.onlyShortlisted == true) // Remove in only shortlisted page
                        propertySummaryUIList.removeAt(propertyIdx)
                    else
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
    private fun reset() {
        offset = 0
        hasMore = true
        propertySummaryUIList.clear()
        _propertySummariesResult.value = ResultUI.Success(emptyList())
    }
}

class PropertiesListViewModelFactory(
    private val propertyUC: PropertyUseCase,
    private val propertyUserActionUC: TenantRelatedPropertyUseCase,
    private val searchHistoryUC: SearchHistoryUseCase,
    private val currentUser: User
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PropertiesListViewModel::class.java))
            return PropertiesListViewModel(
                propertyUC,
                propertyUserActionUC,
                searchHistoryUC,
                currentUser
            ) as T

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}