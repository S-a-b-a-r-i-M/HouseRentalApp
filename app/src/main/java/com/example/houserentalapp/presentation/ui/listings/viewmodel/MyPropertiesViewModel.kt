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

    fun updatePropertyAvailability(
        propertyId: Long,
        isActive: Boolean,
        onSuccess: (Boolean) -> Unit,
        onFailure: () -> Unit
    ) {
        val propertyIdx = propertySummaryUIList.indexOfFirst { it.summary.id == propertyId }
        if (propertyIdx == -1)  {
            logWarning("property $propertyId is not found")
            onFailure()
            return
        }

        viewModelScope.launch {
            val summary = propertySummaryUIList[propertyIdx].summary
            when(propertyUC.updatePropertyAvailability(
                summary.id,
                isActive
            )) {
                is Result.Success<*> -> {
                    propertySummaryUIList[propertyIdx] = propertySummaryUIList[propertyIdx].copy(
                        summary = summary.copy(isActive = isActive)
                    )
                    _propertySummariesResult.value = ResultUI.Success(propertySummaryUIList)
                    onSuccess(isActive)
                }
                is Result.Error -> {
                    logError("Error while toggling property to shortlisted")
                    onFailure()
                }
            }
        }
    }

    fun deleteProperty(propertyId: Long, onSuccess: () -> Unit, onFailure: () -> Unit) {
        val propertyIdx = propertySummaryUIList.indexOfFirst { it.summary.id == propertyId }
        if (propertyIdx == -1)  {
            logWarning("property $propertyId is not found")
            onFailure()
            return
        }

        viewModelScope.launch {
            val summary = propertySummaryUIList[propertyIdx].summary
            when(propertyUC.deleteProperty(summary.id, currentUser.id)) {
                is Result.Success<*> -> {
                    propertySummaryUIList.removeAt(propertyIdx)
                    _propertySummariesResult.value = ResultUI.Success(propertySummaryUIList)
                    onSuccess()
                }
                is Result.Error -> {
                    logError("Error while deleting property")
                    onFailure()
                }
            }
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
