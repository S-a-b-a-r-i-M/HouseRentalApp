package com.example.houserentalapp.presentation.ui.sharedviewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.houserentalapp.data.repo.UserRepoImpl
import com.example.houserentalapp.domain.model.ImageSource
import com.example.houserentalapp.domain.model.PropertyFilters
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.usecase.UserUseCase
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.logWarning
import kotlinx.coroutines.launch

class SharedDataViewModel(private val userUC: UserUseCase? = null) : ViewModel() {
    lateinit var currentUserData: User
        private set
    private val _currentUserLD = MutableLiveData<User>()
    val currentUserLD: LiveData<User> = _currentUserLD
    private val _logOutUser = MutableLiveData(false)
    val logOutUser: LiveData<Boolean> = _logOutUser
    private val _updatedPropertyId = MutableLiveData<Long?>(null)
    val updatedPropertyId: LiveData<Long?> = _updatedPropertyId

    fun loadCurrentUser(userId: Long, onResult: (Boolean) -> Unit) {
        if (userUC == null) {
            logWarning("To loadCurrentUser UserUseCase is required.")
            return
        }
        viewModelScope.launch {
            when(val res = userUC.getUserById(userId)) {
                is Result.Success<User> -> {
                    logInfo("CurrentUser Fetched Successfully: ${res.data}")
                    currentUserData = res.data
                    _currentUserLD.value = res.data
                    onResult(true)
                }
                is Result.Error -> {
                    logError("Fetching current user failed")
                    onResult(false)
                }
            }
        }
    }

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

    fun setUpdatedPropertyId(propertyId: Long) {
        _updatedPropertyId.value = propertyId
    }

    fun clearUpdatedPropertyId() {
        _updatedPropertyId.value = null
    }

    var imageSources: List<ImageSource> = emptyList()
}

class SharedDataViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(SharedDataViewModel::class.java)) {
            val userUC = UserUseCase(UserRepoImpl(context))
            return SharedDataViewModel(userUC) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}