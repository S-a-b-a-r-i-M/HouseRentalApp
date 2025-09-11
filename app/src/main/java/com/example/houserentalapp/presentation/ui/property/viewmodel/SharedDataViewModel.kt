package com.example.houserentalapp.presentation.ui.property.viewmodel

import androidx.lifecycle.ViewModel
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.presentation.utils.extensions.logDebug

class SharedDataViewModel : ViewModel() {
    lateinit var currentUser: User
        private set

    private val _fPropertiesListStore = mutableMapOf<String, Any>()
    val propertiesListStore: Map<String, Any> = _fPropertiesListStore

    private val _fSearchViewStore = mutableMapOf<String, Any>()
    val fSearchViewStore: Map<String, Any> = _fSearchViewStore

    private val fSinglePropertyDetailMap = mutableMapOf<String, Any>()
    private val fCreatePropertyMap = mutableMapOf<String, Any>()

    fun addToPropertiesListStore(key: String, value: Any) {
        _fPropertiesListStore[key] = value
    }

    fun resetPropertiesListStore() {
        _fPropertiesListStore.clear()
    }

    fun addToSearchViewStore(key: String, value: Any) {
        _fSearchViewStore[key] = value
    }

    fun resetSearchViewStore() {
        _fSearchViewStore.clear()
    }

    fun setCurrentUser(user: User) {
        logDebug("CurrentUser is initialized: $user")
        currentUser = user
    }
}