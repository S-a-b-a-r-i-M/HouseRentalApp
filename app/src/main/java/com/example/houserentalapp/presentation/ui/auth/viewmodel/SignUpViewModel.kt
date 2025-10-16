package com.example.houserentalapp.presentation.ui.auth.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.usecase.UserUseCase
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.model.NewUserUI
import com.example.houserentalapp.presentation.ui.auth.AuthDependencyStore
import com.example.houserentalapp.presentation.utils.ResultUI
import kotlinx.coroutines.launch

class SignUpViewModel(private val userUC: UserUseCase) : ViewModel() {
    private val _signUpResult = MutableLiveData<ResultUI<User>>()
    val signUpResult: LiveData<ResultUI<User>> = _signUpResult

    fun signUp(newUserUI: NewUserUI) {
        _signUpResult.value = ResultUI.Loading
        viewModelScope.launch {
            when(val res = userUC.createUser(newUserUI.toDomainUser())) {
                is Result.Success<User> -> {
                    _signUpResult.value = ResultUI.Success(res.data)
                }
                is Result.Error -> {
                    _signUpResult.value = ResultUI.Error(res.message)
                }
            }
        }
    }
}


class SignUpViewModelFactory(private val dependencyStore: AuthDependencyStore) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignUpViewModel::class.java))
            return SignUpViewModel(dependencyStore.userUC) as T

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}