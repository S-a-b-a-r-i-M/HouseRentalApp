package com.example.houserentalapp.domain.usecase

import com.example.houserentalapp.domain.model.Pagination
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.domain.model.enums.UserActionEnum
import com.example.houserentalapp.domain.repo.UserPropertyRepo
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.extensions.logError

class PropertyShortlistsUseCase(private val userPropertyRepo: UserPropertyRepo) {
    suspend fun addToShortlists(currentUserId: Long, propertyId: Long): Result<Long> {
        return try {
            return userPropertyRepo.storeUserAction(
                currentUserId, propertyId, UserActionEnum.SHORTLISTED
            )
        } catch (exp: Exception) {
            logError("${exp.message} while add property to shortlists")
            Result.Error(exp.message.toString())
        }
    }

    suspend fun getShortlistsByUser(
        currentUserId: Long, pagination: Pagination
    ): Result<List<PropertySummary>> {
        return try {
            return userPropertyRepo.getPropertyListByUserAction(
                currentUserId, pagination, UserActionEnum.SHORTLISTED
            )
        } catch (exp: Exception) {
            logError("${exp.message} while add property to shortlists")
            Result.Error(exp.message.toString())
        }
    }

    suspend fun removeFromShortlists(currentUserId: Long, propertyId: Long): Result<Boolean> {
        return try {
            return userPropertyRepo.deleteUserAction(
                currentUserId, propertyId, UserActionEnum.SHORTLISTED
            )
        } catch (exp: Exception) {
            logError("${exp.message} while add property to shortlists")
            Result.Error(exp.message.toString())
        }
    }
}