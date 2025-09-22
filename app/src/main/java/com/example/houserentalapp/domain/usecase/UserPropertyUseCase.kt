package com.example.houserentalapp.domain.usecase

import com.example.houserentalapp.domain.model.Lead
import com.example.houserentalapp.domain.model.Pagination
import com.example.houserentalapp.domain.model.UserActionData
import com.example.houserentalapp.domain.model.enums.LeadStatus
import com.example.houserentalapp.domain.model.enums.LeadUpdatableField
import com.example.houserentalapp.domain.model.enums.UserActionEnum
import com.example.houserentalapp.domain.repo.UserPropertyRepo
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo

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

    suspend fun createLead(tenantId: Long, landlordId: Long, propertyId: Long): Result<Long> {
        return try {
            userPropertyRepo.createLead(tenantId, landlordId, propertyId, LeadStatus.NEW)
        } catch (exp: Exception) {
            logError("${exp.message.toString()} while create Lead")
            Result.Error(exp.message.toString())
        }
    }

    suspend fun getLeadsByLandlord(landlordId: Long, pagination: Pagination): Result<List<Lead>> {
        return try {
            userPropertyRepo.getLeadsByLandlord(landlordId, pagination)
        } catch (exp: Exception) {
            logError("${exp.message.toString()} while reading Leads")
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

    suspend fun updateLead(
        leadId: Long, updateData: Map<LeadUpdatableField, String>
    ): Result<Unit> {
        return try {
            when(val res = userPropertyRepo.updateLead(leadId, updateData)) {
                is Result.Success<Boolean> -> {
                    if (res.data) {
                        logInfo("Update lead($leadId) success.")
                        Result.Success(Unit)
                    }
                    else {
                        logError("Update Lead failed, received success with false from repo")
                        Result.Error("update lead failed")
                    }
                }
                is Result.Error -> res
            }
        } catch (exp: Exception) {
            logError("${exp.message.toString()} while updateLead(id: $leadId)")
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