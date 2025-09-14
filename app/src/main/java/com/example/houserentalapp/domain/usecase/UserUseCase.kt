package com.example.houserentalapp.domain.usecase

import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.model.enums.UserField
import com.example.houserentalapp.domain.repo.UserRepo
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.extensions.logError

class UserUseCase(private val userRepo: UserRepo) {
    suspend fun getUserByPhone(phone: String): Result<User?> {
        return try {
            return userRepo.getUserByPhone(phone)
        } catch (exp: Exception) {
            logError("${exp.message.toString()} while fetching property summaries")
            Result.Error(exp.message.toString())
        }
    }

    suspend fun updateUser(modifiedUser: User, updatedFields: List<UserField>): Result<User> {
        return try {
            return userRepo.updateUser(modifiedUser, updatedFields)
        } catch (exp: Exception) {
            logError("${exp.message.toString()} while fetching property summaries")
            Result.Error(exp.message.toString())
        }
    }
}