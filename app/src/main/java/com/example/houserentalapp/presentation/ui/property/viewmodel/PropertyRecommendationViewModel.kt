package com.example.houserentalapp.presentation.ui.property.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.houserentalapp.data.repo.PropertyRepoImpl
import com.example.houserentalapp.data.repo.UserPropertyRepoImpl
import com.example.houserentalapp.domain.model.Pagination
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.model.enums.UserActionEnum
import com.example.houserentalapp.domain.usecase.PropertyUseCase
import com.example.houserentalapp.domain.usecase.UserPropertyUseCase
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.model.PropertySummaryUI
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.logWarning
import kotlinx.coroutines.launch

class PropertyRecommendationViewModel(
    private val propertyUC: PropertyUseCase,
    private val userPropertyUC: UserPropertyUseCase,
    private val currentUser: User
) : ViewModel() {
    private val _propertySummariesResult = MutableLiveData<ResultUI<List<PropertySummaryUI>>>()
    val propertySummariesResult: LiveData<ResultUI<List<PropertySummaryUI>>> = _propertySummariesResult
    private lateinit var summaryUIList: MutableList<PropertySummaryUI>

    fun loadPropertySummaries(propertyId: Long) {
        _propertySummariesResult.value = ResultUI.Loading
        viewModelScope.launch {
            try {
                val result = propertyUC.getPropertySummaries(
                    currentUser.id, Pagination(propertyId.toInt() + 1, 10)
                )
                when (result) {
                    is Result.Success<List<Pair<PropertySummary, Boolean>>> -> {
                        summaryUIList = result.data.map {
                            PropertySummaryUI(summary = it.first, isShortListed = it.second)
                        }.toMutableList()
                        _propertySummariesResult.value = ResultUI.Success(summaryUIList)
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

    fun toggleShortlist(propertyId: Long, onSuccess: (Boolean) -> Unit, onFailure: () -> Unit) {
        val propertyIdx = summaryUIList.indexOfFirst { it.summary.id == propertyId }
        if (propertyIdx == -1)  {
            logWarning("At toggleShortlist, property $propertyId is not found")
            onFailure()
            return
        }

        viewModelScope.launch {
            val summaryUI = summaryUIList[propertyIdx]
            val result = if (summaryUI.isShortListed)
                userPropertyUC.deleteUserAction(
                    currentUser.id, propertyId, UserActionEnum.SHORTLISTED
                )
            else
                userPropertyUC.storeTenantAction(
                    currentUser.id, propertyId, UserActionEnum.SHORTLISTED
                )

            when (result) {
                is Result.Success<*> -> {
                    logInfo("property shortlist state toggled")
                    val newState = !summaryUI.isShortListed
                    summaryUIList[propertyIdx] = summaryUI.copy(isShortListed = newState)

                    _propertySummariesResult.value = ResultUI.Success(summaryUIList)
                    onSuccess(newState)
                }

                is Result.Error -> {
                    logError("Error while toggling property to shortlisted")
                    onFailure()
                }
            }
        }
    }
}


class PropertyRecommendationViewModelFactory(
    private val context: Context,
    private val currentUser: User
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PropertyRecommendationViewModel::class.java)) {
            val propertyUC = PropertyUseCase(PropertyRepoImpl(context))
            val userPropertyUC = UserPropertyUseCase(UserPropertyRepoImpl(context))
            return PropertyRecommendationViewModel(
                propertyUC, userPropertyUC, currentUser
            ) as T
        }

        throw IllegalArgumentException("Unknown View Model")
    }
}