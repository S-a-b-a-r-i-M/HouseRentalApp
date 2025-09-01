package com.example.houserentalapp.data.repo

import android.content.Context
import com.example.houserentalapp.data.local.db.DatabaseHelper
import com.example.houserentalapp.data.local.db.dao.UserPropertyDao
import com.example.houserentalapp.data.mapper.PropertyMapper
import com.example.houserentalapp.domain.model.Pagination
import com.example.houserentalapp.domain.model.PropertyLead
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.domain.model.UserActionData
import com.example.houserentalapp.domain.model.enums.UserActionEnum
import com.example.houserentalapp.domain.repo.UserPropertyRepo
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.logWarning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class UserPropertyRepoImpl(private val context: Context) : UserPropertyRepo {
    private val userPropertyDao = UserPropertyDao(DatabaseHelper.getInstance(context))

    // -------------- CREATE --------------
    override suspend fun storeUserAction(
        userId: Long,
        propertyId: Long,
        action: UserActionEnum
    ): Result<Long> {
        return try {
            withContext(Dispatchers.IO) {
                val id = userPropertyDao.insertUserAction(userId, propertyId, action)
                logInfo("Property($propertyId) added to User's($userId) ${action.name} list")
                Result.Success(id)
            }
        } catch (exp: Exception) {
            logError("Error inserting property User Action", exp)
            Result.Error(exp.message.toString())
        }
    }

    override suspend fun storeInterestedProperty(
        userId: Long, propertyId: Long
    ): Result<PropertyLead> {
        TODO("Not yet implemented")
    }

    // -------------- READ --------------
    override suspend fun getPropertyListByUserAction(
        userId: Long, pagination: Pagination, action: UserActionEnum
    ): Result<List<PropertySummary>> {
        return try {
            val summariesEntity = userPropertyDao.getPropertySummariesByUserAction(
                userId, pagination, action
            )
            logDebug("PropertiesByUserAction: ${action.name} count ${summariesEntity.size}")

            // Convert to domain
            val summariesDomain = summariesEntity.map { PropertyMapper.toPropertySummaryDomain(it) }
            return Result.Success(summariesDomain)
        } catch (exp: Exception) {
            logError("Error reading PropertyListByUserAction", exp)
            Result.Error(exp.message.toString())
        }
    }

    override suspend fun getUserActions(userId: Long): Result<List<UserActionData>> {
        TODO("Not yet implemented")
    }

    override suspend fun getLeads(
        landlordId: Long, pagination: Pagination
    ): Result<List<PropertyLead>> {
        TODO("Not yet implemented")
    }

    // -------------- UPDATE --------------
    override suspend fun updateLead(
        leadId: Long, updateFields: Map<String, Any>
    ): Result<Boolean> {
        TODO("Not yet implemented")
    }

    // -------------- DELETE --------------
    override suspend fun deleteUserAction(
        userId: Long,
        propertyId: Long,
        action: UserActionEnum
    ): Result<Boolean> {
        if (!(action == UserActionEnum.SHORTLISTED || action == UserActionEnum.INTERESTED)) {
            logWarning("User Action(${action.name} can't be deleted")
            return Result.Error("Action can't be performed")
        }

        return try {
            val isRemoved = userPropertyDao.removeFromShortlists(
                userId, propertyId, UserActionEnum.SHORTLISTED
            )
            logInfo("Property deleteFromShortlists result: $isRemoved")
            Result.Success(isRemoved)
        } catch (exp: Exception) {
            logError("Error deleting property($propertyId) from shortlists", exp)
            Result.Error(exp.message.toString())
        }
    }
}