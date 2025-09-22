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
import com.example.houserentalapp.domain.usecase.UserPropertyUseCase
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.model.PropertyUI
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import kotlinx.coroutines.launch

class SinglePropertyDetailViewModel(
    private val propertyUC: PropertyUseCase,
    private val userPropertyUC: UserPropertyUseCase,
    private val currentUser: User
) : ViewModel() {
    private val _onlyPropertyDetailsRes = MutableLiveData<ResultUI<Property>>()
    val onlyPropertyDetailsRes: LiveData<ResultUI<Property>> = _onlyPropertyDetailsRes

    private val _propertyUIResult = MutableLiveData<ResultUI<PropertyUI>>()
    val propertyUIResult: LiveData<ResultUI<PropertyUI>> = _propertyUIResult

    private var property: Property? = null
    private lateinit var propertyUI: PropertyUI

    fun clearChangeFlags() {
        val res = propertyUIResult.value
        if (res is ResultUI.Success<PropertyUI>) res.data.resetFlags()
    }

    fun loadProperty(propertyId: Long) {
        _onlyPropertyDetailsRes.value = ResultUI.Loading
        viewModelScope.launch {
            try {
                when (val result = propertyUC.getProperty(propertyId)) {
                    is Result.Success<Property> -> {
                        logInfo("successfully loaded property(id: $propertyId)")
                        property = result.data
                        _onlyPropertyDetailsRes.value = ResultUI.Success(result.data)
                    }
                    is Result.Error -> {
                        _onlyPropertyDetailsRes.value = ResultUI.Error(result.message)
                        logError("Error on loadProperty : ${result.message}")
                    }
                }
            } catch (exp: Exception) {
                _onlyPropertyDetailsRes.value = ResultUI.Error("Unexpected Error")
                logError("Error on loadProperty : ${exp.message}")
            }
        }
    }

    fun loadPropertyWithActions(propertyId: Long) {
        viewModelScope.launch {
            when (val res = userPropertyUC.getPropertyWithActions(
                currentUser.id, propertyId
            )) {
                is Result.Success<Map<String, Any>> -> {
                    logInfo("successfully loaded user actions for property")
                    val property = res.data.getValue("property") as Property
                    val actions = res.data.getValue("actions") as? List<UserActionData>
                    val landlordUser = res.data.getValue("landlordUser") as User

                    var isShortlisted = false
                    var isInterested = false
                    actions?.forEach {
                        if (it.action == UserActionEnum.SHORTLISTED)
                            isShortlisted = true
                        else if (it.action == UserActionEnum.INTERESTED)
                            isInterested = true
                    }
                    propertyUI = PropertyUI(
                        property,
                        isShortlisted,
                        isInterested,
                        landlordUser,
                        propertyInfoChanged = true,
                        shortlistStateChanged = true,
                        interestedStateChanged = true,
                        landlordUserInfoChanged = true,
                    )
                    _propertyUIResult.value = ResultUI.Success(propertyUI)
                }
                is Result.Error -> {
                    logError("Error: loading user(${currentUser.id}) actions ${res.message}")
                }
            }
        }
    }

    private fun removeFromShortlists(propertyId: Long) {
        viewModelScope.launch {
            when(userPropertyUC.deleteUserAction(
                currentUser.id, propertyId, UserActionEnum.SHORTLISTED
            )) {
                is Result.Success<Boolean> -> {
                    logInfo("property removed from shortlisted")
                    propertyUI = propertyUI.copy(isShortlisted = false, shortlistStateChanged = true)
                    _propertyUIResult.value = ResultUI.Success(propertyUI)
                }
                is Result.Error -> {
                    logError("Error while removing property from shortlisted")
                }
            }
        }
    }

    private fun addToShortlists(propertyId: Long) {
        viewModelScope.launch {
            when (userPropertyUC.storeTenantAction(
                currentUser.id, propertyId, UserActionEnum.SHORTLISTED
            )) {
                is Result.Success<*> -> {
                    logInfo("property added to shortlisted")
                    propertyUI = propertyUI.copy(isShortlisted = true, shortlistStateChanged = true)
                    _propertyUIResult.value = ResultUI.Success(propertyUI)
                }
                is Result.Error -> {
                    logError("Error while adding property to shortlisted")
                }
            }
        }
    }

    fun storeUserInterest(propertyId: Long) {
        viewModelScope.launch {
            when (userPropertyUC.storeTenantAction(
                currentUser.id, propertyId, UserActionEnum.INTERESTED
            )) {
                is Result.Success<*> -> {
                    logInfo("property added to INTERESTED")
                    propertyUI = propertyUI.copy(isInterested = true, interestedStateChanged = true)
                    _propertyUIResult.value = ResultUI.Success(propertyUI)
                    // Create Lead
                    createLead(
                        currentUser.id,
                        propertyUI.property.landlordId,
                        propertyId
                    )
                }
                is Result.Error -> {
                    logError("Error while adding property to INTERESTED")
                }
            }
        }
    }

    private fun createLead(tenantId: Long, landlordId: Long, propertyId: Long) {
        viewModelScope.launch {
            when (userPropertyUC.createLead(tenantId, landlordId, propertyId)) {
                is Result.Success<*> -> {
                    logInfo("Lead added")
                }
                is Result.Error -> {
                    logError("Error while adding lead")
                }
            }
        }
    }

    fun toggleFavourite(propertyId: Long) {
        if (propertyUI.isShortlisted)
            removeFromShortlists(propertyId)
        else // Add to favourites
            addToShortlists(propertyId)
    }
}

class SinglePropertyDetailViewModelFactory(
    private val propertyUC: PropertyUseCase,
    private val userPropertyUC: UserPropertyUseCase,
    private val currentUser: User
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SinglePropertyDetailViewModel::class.java))
            return SinglePropertyDetailViewModel(
                propertyUC,
                userPropertyUC,
                currentUser
            ) as T

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}