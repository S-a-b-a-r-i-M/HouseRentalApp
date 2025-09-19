package com.example.houserentalapp.domain.usecase

import com.example.houserentalapp.domain.model.UserActionData
import com.example.houserentalapp.domain.model.enums.UserActionEnum
import com.example.houserentalapp.domain.repo.UserPropertyRepo
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.extensions.logError

class UserPropertyUseCase(private val userPropertyRepo: UserPropertyRepo) {
    suspend fun storeTenantAction(
        currentUserId: Long,
        propertyId: Long,
        action: UserActionEnum
    ): Result<Long> {
        return try {
            userPropertyRepo.storeUserAction(currentUserId, propertyId, action)
        } catch (exp: Exception) {
            logError("${exp.message.toString()} while fetching property(id: $propertyId)")
            Result.Error(exp.message.toString())
        }
    }

    suspend fun getPropertyUserActions(
        currentUserId: Long, propertyId: Long
    ): Result<List<UserActionData>> {
        return try {
            return userPropertyRepo.getUserActions(currentUserId, listOf(propertyId))
        } catch (exp: Exception) {
            logError("${exp.message.toString()} while fetching property(id: $propertyId)")
            Result.Error(exp.message.toString())
        }
    }

    suspend fun getPropertyWithActions(
        currentUserId: Long, propertyId: Long
    ): Result<Map<String, Any>> {
        return try {
            userPropertyRepo.getPropertyWithUserActions(currentUserId, propertyId)
        } catch (exp: Exception) {
            logError("${exp.message.toString()} while fetching property(id: $propertyId)")
            Result.Error(exp.message.toString())
        }
    }

    suspend fun deleteUserAction(
        currentUserId: Long,
        propertyId: Long,
        action: UserActionEnum
    ): Result<Boolean> {
        return try {
            userPropertyRepo.deleteUserAction(currentUserId, propertyId, action)
        } catch (exp: Exception) {
            logError("${exp.message.toString()} while fetching property(id: $propertyId)")
            Result.Error(exp.message.toString())
        }
    }
}