package com.example.houserentalapp.presentation.ui.auth

import android.content.Context
import com.example.houserentalapp.data.repo.UserRepoImpl
import com.example.houserentalapp.domain.usecase.UserUseCase

// Manual Dependency Injection
class AuthDependencyStore(context: Context) {
    val userUC by lazy {
        val userRepoImpl = UserRepoImpl(context)
        UserUseCase(userRepoImpl)
    }
}