package com.example.houserentalapp.data.repo

import android.content.Context
import com.example.houserentalapp.data.local.db.DatabaseHelper
import com.example.houserentalapp.data.local.db.dao.PropertyDao
import com.example.houserentalapp.data.local.db.dao.UserDao
import com.example.houserentalapp.data.local.db.dao.UserPropertyDao
import com.example.houserentalapp.data.local.db.entity.NewLeadEntity
import com.example.houserentalapp.data.mapper.PropertyMapper
import com.example.houserentalapp.data.mapper.UserActionMapper
import com.example.houserentalapp.domain.model.Pagination
import com.example.houserentalapp.domain.model.Lead
import com.example.houserentalapp.domain.model.UserActionData
import com.example.houserentalapp.domain.model.enums.LeadStatus
import com.example.houserentalapp.domain.model.enums.LeadUpdatableField
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

class UserPropertyRepoImpl(context: Context) : UserPropertyRepo {
    private val dbHelper = DatabaseHelper.getInstance(context)
    private val userDao = UserDao(dbHelper)
    private val propertyDao = PropertyDao(dbHelper)
    private val userPropertyDao = UserPropertyDao(dbHelper)

    // -------------- CREATE --------------
    override suspend fun storeUserAction(
        userId: Long,
        propertyId: Long,
        action: UserActionEnum
    ): Result<Long> {
        return try {
            withContext(Dispatchers.IO) {
                val id = userPropertyDao.storeUserAction(userId, propertyId, action)
                logInfo("Property($propertyId) added to User's($userId) ${action.name} list")
                Result.Success(id)
            }
        } catch (exp: Exception) {
            logError("Error inserting property User Action", exp)
            Result.Error(exp.message.toString())
        }
    }

    override suspend fun createLead(
        tenantId: Long,
        landlordId: Long,
        propertyId: Long,
        status: LeadStatus,
    ): Result<Long> {
        return try {
            withContext(Dispatchers.IO) {
                // Check is lead is already exists
                var leadId = userPropertyDao.getLeadId(landlordId, tenantId)
                logDebug("Retrieved lead id is : $leadId")
                if (leadId == null) {
                    // Create Lead
                    leadId = userPropertyDao.storeLead(
                        NewLeadEntity(
                            tenantId = tenantId,
                            landlordId = landlordId,
                            propertyId = propertyId,
                            status = status.readable,
                        )
                    )
                    logDebug("Lead($leadId) created")
                }

                // Map Lead With Property
                logInfo("Lead($leadId) Mapped to Property($propertyId)")
                userPropertyDao.mapLeadToProperty(leadId, propertyId)

                Result.Success(leadId)
            }
        } catch (exp: Exception) {
            logError("Error inserting property User Action", exp)
            Result.Error(exp.message.toString())
        }
    }

    // -------------- READ --------------
    override suspend fun getPropertyWithUserActions(
        userId: Long, propertyId: Long
    ) : Result<Map<String, Any>> {
        return try {
            withContext(Dispatchers.IO) {
                // Get Property
                val property = PropertyMapper.toDomain(
                    propertyDao.getPropertyById(propertyId)
                )
                // Get Actions
                val userActions = userPropertyDao.getUserActions(userId, propertyId).map {
                    UserActionMapper.toDomain(it)
                }
                // Get Landlord Details
                val userDomain = userDao.getUserById(property.landlordId)

                Result.Success(mapOf(
                    "property" to property,
                    "actions" to userActions,
                    "landlordUser" to userDomain
                ))
            }
        } catch (exp: Exception) {
            logError("Error reading UserActions", exp)
            Result.Error(exp.message.toString())
        }
    }

    override suspend fun getUserActions(
        userId: Long, propertyIds: List<Long>
    ): Result<List<UserActionData>> {
        return try {
            withContext(Dispatchers.IO) {
                val userActionEntity = userPropertyDao.getUserActions(userId, propertyIds)
                logDebug("getUserActions: count ${userActionEntity.size}")

                // Convert to domain
                val userActionDomain = userActionEntity.map { UserActionMapper.toDomain(it) }
                Result.Success(userActionDomain)
            }
        } catch (exp: Exception) {
            logError("Error reading UserActions", exp)
            Result.Error(exp.message.toString())
        }
    }

    override suspend fun getLeadsByLandlord(landlordId: Long, pagination: Pagination): Result<List<Lead>>  {
        return try {
            withContext(Dispatchers.IO) {
                val leadEntityList = userPropertyDao.getLeads(landlordId, pagination)
                logDebug("getUserActions: count ${leadEntityList.size}")

                // Get Property Summaries
                val leadDomainList = mutableListOf<Lead>()
                if (leadEntityList.isNotEmpty()) {
                    val uniquePropertyIds = mutableSetOf<Long>()
                    leadEntityList.forEach {
                        uniquePropertyIds.addAll(it.interestedPropertyIds)
                    }
                    val propertySummariesMap = propertyDao.getPropertySummariesById(
                        uniquePropertyIds.toList()
                    ).associateBy { it.id }

                    // Add Interested Properties into each lead data
                    leadEntityList.forEach {
                        val interestedProperties = it.interestedPropertyIds.mapNotNull { propertyId ->
                            propertySummariesMap[propertyId]
                        }.map {
                            PropertyMapper.toPropertySummaryDomain(it)
                        }
                        leadDomainList.add(
                            Lead(
                                id = it.id,
                                leadUser = it.lead,
                                interestedProperties = interestedProperties,
                                status = LeadStatus.fromString(it.status),
                                note = it.note,
                                createdAt = it.createdAt
                            )
                        )
                    }
                }
                
                Result.Success(leadDomainList)
            }
        } catch (exp: Exception) {
            logError("Error reading Leads", exp)
            Result.Error(exp.message.toString())
        }
    }

    // -------------- UPDATE --------------
    override suspend fun updateLead(
        leadId: Long,
        updateData: Map<LeadUpdatableField, String>
    ): Result<Boolean> {
        return try {
            withContext(Dispatchers.IO) {
                if (updateData.isEmpty())
                    return@withContext Result.Success(true)

                val updatedRows = userPropertyDao.updateLead(leadId, updateData)
                logDebug("UpdateLead -> updated rows count is $updatedRows")
                Result.Success(updatedRows > 0)
            }
        } catch (exp: Exception) {
            logError("Error updateLead($leadId)", exp)
            Result.Error(exp.message.toString())
        }
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