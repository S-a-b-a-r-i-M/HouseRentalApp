package com.example.houserentalapp.presentation.ui.property.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.houserentalapp.domain.model.Lead
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.presentation.utils.extensions.logDebug

class SharedDataViewModel : ViewModel() {
    lateinit var currentUserData: User
        private set
    private val _currentUserLD = MutableLiveData<User>()
    val currentUserLD: LiveData<User> = _currentUserLD

    var currentLead: Lead? = null

    private val _fPropertiesListStore = mutableMapOf<String, Any>()
    val propertiesListStore: Map<String, Any> = _fPropertiesListStore

    fun addToPropertiesListStore(key: String, value: Any) {
        _fPropertiesListStore[key] = value
    }

    fun resetPropertiesListStore() {
        _fPropertiesListStore.clear()
    }

    fun setCurrentUser(user: User) {
        logDebug("CurrentUser is set: $user")
        currentUserData = user
        _currentUserLD.value = user
    }
}