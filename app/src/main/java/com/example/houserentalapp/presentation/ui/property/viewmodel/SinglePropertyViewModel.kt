package com.example.houserentalapp.presentation.ui.property.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.houserentalapp.domain.model.Property
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.model.UserActionData
import com.example.houserentalapp.domain.model.enums.UserActionEnum
import com.example.houserentalapp.domain.usecase.PropertyUseCase
import com.example.houserentalapp.domain.usecase.TenantRelatedPropertyUseCase
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.model.PropertyWithActionsUI
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.logWarning
import kotlinx.coroutines.launch

class SinglePropertyDetailViewModel(
    private val getPropertyUC: PropertyUseCase,
    private val propertyUserActionUC: TenantRelatedPropertyUseCase,
    private val currentUser: User
) : ViewModel() {
    private val _propertyResult = MutableLiveData<ResultUI<PropertyWithActionsUI>>()
    val propertyResult: LiveData<ResultUI<PropertyWithActionsUI>> = _propertyResult

    private var _isShortlisted = MutableLiveData<Boolean>(false)
    var isShortlisted: LiveData<Boolean> = _isShortlisted

    fun loadProperty(propertyId: Long, withTenantActions: Boolean = false) {
        _propertyResult.value = ResultUI.Loading
        viewModelScope.launch {
            try {
                when (val result = getPropertyUC.getProperty(propertyId)) {
                    is Result.Success<Property> -> {
                        logInfo("successfully loaded property(id: $propertyId)")
                        val property = result.data
                        if (withTenantActions)
                            loadPropertyActions(currentUser.id, property)
                        else
                            _propertyResult.value = ResultUI.Success(PropertyWithActionsUI(property))
                    }
                    is Result.Error -> {
                        _propertyResult.value = ResultUI.Error(result.message)
                        logError("Error on loadProperty : ${result.message}")
                    }
                }
            } catch (exp: Exception) {
                _propertyResult.value = ResultUI.Error("Unexpected Error")
                logError("Error on loadProperty : ${exp.message}")
            }
        }
    }

    private fun loadPropertyActions(userId: Long, property: Property) {
        if (property.id == null) {
            logWarning("loadPropertyActions -> Property Id not found")
            return
        }

        viewModelScope.launch {
            when (val res = propertyUserActionUC.getPropertyUserActions(userId, property.id)) {
                is Result.Success<List<UserActionData>> -> {
                    logInfo("successfully loaded user actions for property")
                    _propertyResult.value = ResultUI.Success(
                        PropertyWithActionsUI(property, res.data)
                    )
                    updateIsShortlisted(res.data)
                }
                is Result.Error -> {
                    _propertyResult.value = ResultUI.Success(PropertyWithActionsUI(property))
                    logError("Error on loading user($userId) actions ${res.message}")
                }
            }
        }
    }

    private fun updateIsShortlisted(actionDataList: List<UserActionData>) {
        _isShortlisted.value = actionDataList.any { it.action == UserActionEnum.SHORTLISTED }
    }

    private fun removeFromShortlists(propertyId: Long) {
        viewModelScope.launch {
            when(propertyUserActionUC.deleteUserAction(
                currentUser.id, propertyId, UserActionEnum.SHORTLISTED
            )) {
                is Result.Success<Boolean> -> {
                    logInfo("property removed from shortlisted")
                    _isShortlisted.value = false
                }
                is Result.Error -> {
                    logError("Error while removing property from shortlisted")
                }
            }
        }
    }

    private fun addToShortlists(propertyId: Long) {
        viewModelScope.launch {
            when (propertyUserActionUC.storeUserAction(
                currentUser.id, propertyId, UserActionEnum.SHORTLISTED
            )) {
                is Result.Success<*> -> {
                    logInfo("property added to shortlisted")
                    _isShortlisted.value = true
                }
                is Result.Error -> {
                    logError("Error while adding property to shortlisted")
                }
            }
        }
    }

    fun toggleFavourite(propertyId: Long) {
        if (isShortlisted.value!!)
            removeFromShortlists(propertyId)
        else // Add to favourites
            addToShortlists(propertyId)
    }
}

class SinglePropertyDetailViewModelFactory(
    private val getPropertyUseCase: PropertyUseCase,
    private val propertyUserActionUseCase: TenantRelatedPropertyUseCase,
    private val currentUser: User
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SinglePropertyDetailViewModel::class.java))
            return SinglePropertyDetailViewModel(
                getPropertyUseCase,
                propertyUserActionUseCase,
                currentUser
            ) as T

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}