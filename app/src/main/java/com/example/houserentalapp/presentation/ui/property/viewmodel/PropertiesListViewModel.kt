package com.example.houserentalapp.presentation.ui.property.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.houserentalapp.domain.model.Pagination
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.domain.usecase.GetPropertySummariesUseCase
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logError
import kotlinx.coroutines.launch
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.extensions.logWarning

class PropertiesListViewModel(private val useCase: GetPropertySummariesUseCase) : ViewModel() {
    private val _propertySummariesState = MutableLiveData<ResultUI<List<PropertySummary>>>()
    val propertySummariesState: LiveData<ResultUI<List<PropertySummary>>> = _propertySummariesState

    private val propertySummaryList: MutableList<PropertySummary> = mutableListOf() // Used to hold Property Summary Data
    private val filters = MutableLiveData<Map<String, Any>>()
    private var hasMore: Boolean = true
    private var offset: Int = 0
    private val limit: Int = 10

    fun getOffset() = offset
    init {
        // Load Filters
        //
    }

    fun loadPropertySummaries() {
        if (!hasMore) return
        _propertySummariesState.value = ResultUI.Loading
        viewModelScope.launch {
            try {
                when (val result = useCase(filters.value ?: mutableMapOf(), Pagination(offset, limit))) {
                    is Result.Success<List<PropertySummary>> -> {
                        result.data.also {
                            propertySummaryList.addAll(it)
                            _propertySummariesState.value = ResultUI.Success(propertySummaryList)
                            // Does it has more ?
                            val totalRecords = result.meta?.get("total_records")
                            if (totalRecords == null) {
                                logWarning("loadPropertySummaries -> total record count is missing")
                                return@launch
                            }
                            hasMore = propertySummaryList.size < (totalRecords as Int)
                            offset += limit // Update offset
                        }
                    }
                    is Result.Error -> {
                        logError("Error on loadPropertySummaries : ${result.message}")
                        _propertySummariesState.value = ResultUI.Error(result.message)
                    }
                }
            } catch (exp: Exception) {
                logError("Error on loadPropertySummaries : ${exp.message}")
                _propertySummariesState.value = ResultUI.Error("Unexpected Error")
            }
        }
    }

    // Will get triggered by filters modification
    fun resetPagination() {
        offset = 0
        hasMore = true
        propertySummaryList.clear()
        _propertySummariesState.value = ResultUI.Success(emptyList())
    }

    fun hasMore() = hasMore
}


class PropertiesListViewModelFactory(
    private val useCase: GetPropertySummariesUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PropertiesListViewModel::class.java))
            return PropertiesListViewModel(useCase) as T

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}