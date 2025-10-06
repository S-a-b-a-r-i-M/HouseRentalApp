package com.example.houserentalapp.presentation.ui.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.usecase.UserUseCase
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.extensions.logError
import kotlinx.coroutines.launch

class AuthViewModel(private val userUC: UserUseCase) : ViewModel() {
    var isLoading = true
        private set

    fun loadUserIfAlreadyAuthenticated(onSuccess: (User) -> Unit, onFailure: () -> Unit) {
        isLoading = true
        viewModelScope.launch {
            when(val res = userUC.getUserFromSession()) {
                is Result.Success<User?> -> {
                    if (res.data != null)
                        onSuccess(res.data)
                    else
                        onFailure()
                }
                is Result.Error -> {
                    logError("Error at fetchUserByPhone. Error: ")
                    onFailure()
                }
            }
            isLoading = false
        }
    }
}


class AuthViewModelFactory(private val userUC: UserUseCase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java))
            return AuthViewModel(userUC) as T

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}