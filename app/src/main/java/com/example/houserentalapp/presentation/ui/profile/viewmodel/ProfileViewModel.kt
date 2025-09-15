package com.example.houserentalapp.presentation.ui.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.houserentalapp.domain.usecase.UserUseCase
import com.example.houserentalapp.domain.utils.Result
import kotlinx.coroutines.launch

class ProfileViewModel(private val userUseCase: UserUseCase) : ViewModel() {
    fun logOutCurrentUser(userId: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            when (userUseCase.logOutUser(userId)) {
                is Result.Success<*> -> {
                    onResult(true)
                }
                is Result.Error -> {
                    onResult(false)
                }
            }
        }
    }
}

class ProfileViewModelFactory(private val userUC: UserUseCase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java))
            return ProfileViewModel(userUC) as T

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}