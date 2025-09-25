package com.example.houserentalapp.presentation.ui.property.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.houserentalapp.domain.model.ImageSource
import com.example.houserentalapp.domain.model.Lead
import com.example.houserentalapp.domain.model.PropertyFilters
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.presentation.utils.extensions.logDebug

class SharedDataViewModel : ViewModel() {
    lateinit var currentUserData: User
        private set
    private val _currentUserLD = MutableLiveData<User>()
    val currentUserLD: LiveData<User> = _currentUserLD
    private val _logOutUser = MutableLiveData(false)
    val logOutUser: LiveData<Boolean> = _logOutUser

    var currentLead: Lead? = null

    fun setCurrentUser(user: User) {
        logDebug("CurrentUser is set: $user")
        currentUserData = user
        _currentUserLD.value = user
    }

    fun logOutUser() {
        _logOutUser.value = true
    }

    private var currentFilter: PropertyFilters? = null

    fun getAndClearFilters(): PropertyFilters? {
        if (currentFilter == null) return null

        val temp = currentFilter
        currentFilter = null
        return temp
    }

    fun setCurrentFilters(propertyFilters: PropertyFilters) {
        currentFilter = propertyFilters
    }

    var imageSources: List<ImageSource> = emptyList()
}