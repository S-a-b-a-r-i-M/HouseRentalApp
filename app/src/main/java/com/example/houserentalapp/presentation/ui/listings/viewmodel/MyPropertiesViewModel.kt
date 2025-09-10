package com.example.houserentalapp.presentation.ui.listings.viewmodel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.houserentalapp.domain.model.Pagination
import com.example.houserentalapp.domain.model.PropertyFilters
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.usecase.PropertyUseCase
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logError
import kotlinx.coroutines.launch
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.model.PropertySummaryUI
import com.example.houserentalapp.presentation.utils.extensions.logWarning

/* TODO
    1. View Count
 */
class MyPropertiesViewModel(
    private val propertyUC: PropertyUseCase, private val currentUser: User
) : ViewModel() {
    private val _propertySummariesResult = MutableLiveData<ResultUI<List<PropertySummaryUI>>>()
    val propertySummariesResult: LiveData<ResultUI<List<PropertySummaryUI>>> = _propertySummariesResult
    private val propertySummaryUIList: MutableList<PropertySummaryUI> = mutableListOf() // Used to hold Property Summary Data

    var hasMore: Boolean = true
        private set
    private var offset: Int = 0
    private val limit: Int = 10

    fun loadPropertySummaries(filters: PropertyFilters?) {
        if (!hasMore) return
        _propertySummariesResult.value = ResultUI.Loading
        viewModelScope.launch {
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
            return
        }

        viewModelScope.launch {
            val summaryUI = propertySummaryUIList[propertyIdx]
        }
    }
}

class MyPropertiesViewModelFactory(
    private val propertyUC: PropertyUseCase, private val currentUser: User
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyPropertiesViewModel::class.java))
            return MyPropertiesViewModel(propertyUC, currentUser) as T

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
