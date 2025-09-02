package com.example.houserentalapp.domain.repo

import com.example.houserentalapp.domain.model.Pagination
import com.example.houserentalapp.domain.model.PropertyLead
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.domain.model.UserActionData
import com.example.houserentalapp.domain.model.enums.UserActionEnum
import com.example.houserentalapp.domain.utils.Result

/* TODO
 * 1. Implement Search History
 */

interface UserPropertyRepo {
    // CREATE
    suspend fun storeUserAction(userId: Long, propertyId: Long, action: UserActionEnum): Result<Long>

    suspend fun storeInterestedProperty(userId: Long, propertyId: Long): Result<PropertyLead>

    // READ
    suspend fun getPropertySummariesByUserAction(
        userId: Long,
        pagination: Pagination,
        action: UserActionEnum
    ): Result<List<PropertySummary>>

    suspend fun getUserActions(userId: Long, propertyIds: List<Long>): Result<List<UserActionData>>

    suspend fun getLeads(landlordId: Long, pagination: Pagination): Result<List<PropertyLead>> // Have to write join query

    // UPDATE
    suspend fun updateLead(leadId: Long, updateFields: Map<String, Any>): Result<Boolean>

    // DELETE
    suspend fun deleteUserAction(userId: Long, propertyId: Long, action: UserActionEnum): Result<Boolean>
}