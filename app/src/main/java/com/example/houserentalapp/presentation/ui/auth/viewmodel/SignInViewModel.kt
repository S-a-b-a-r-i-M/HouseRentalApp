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

class SignInViewModel(private val userUC: UserUseCase) : ViewModel() {
    private val _signInResult = MutableLiveData<ResultUI<User>>()
    val signInResult: LiveData<ResultUI<User>> = _signInResult

    fun signIn(phone: String, password: String) {
        _signInResult.value = ResultUI.Loading
        viewModelScope.launch {
            when(val res = userUC.getUser(phone, password)) {
                is Result.Success<User> -> {
                    logInfo("User with phone($phone): ${res.data}")
                    _signInResult.value = ResultUI.Success(res.data)
                }
                is Result.Error -> {
                    logError("Error at fetchUserByPhone. Error: ")
                    _signInResult.value = ResultUI.Error(res.message)
                }
            }
        }
    }
}

class SignInViewModelFactory(private val userUC: UserUseCase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignInViewModel::class.java))
            return SignInViewModel(userUC) as T

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}