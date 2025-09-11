package com.example.houserentalapp.presentation.ui.auth.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.usecase.UserUseCase
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import kotlinx.coroutines.launch

class AuthViewModel(private val userUC: UserUseCase) : ViewModel() {
    private val _currentUser = MutableLiveData<ResultUI<User?>>(null)
    val currentUser: LiveData<ResultUI<User?>> = _currentUser

    fun signUp(phone: String) { }

    fun signIn(phone: String) {
        _currentUser.value = ResultUI.Loading
        viewModelScope.launch {
            when(val res = userUC.getUserByPhone(phone)) {
                is Result.Success<User?> -> {
                    logInfo("User with phone($phone): ${res.data}")
                    _currentUser.value = ResultUI.Success(res.data)
                }
                is Result.Error -> {
                    logError("Error at fetchUserByPhone. Error: ")
                    _currentUser.value = ResultUI.Error(
                        "Fetching user details failed, Try again later."
                    )
                }
            }
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