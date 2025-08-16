package com.example.houserentalapp.domain.repo

import com.example.houserentalapp.domain.model.Pagination
import com.example.houserentalapp.domain.model.PropertyLead
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.domain.model.UserActionData
import com.example.houserentalapp.domain.model.enums.UserActionEnum
import com.example.houserentalapp.domain.utils.Result

/* TODO
 * 1. Implement Search History
 * 2. User Activity as a Tenant (viewed, shortlisted, interested)
 */

interface UserPropertyRepo {
    // CREATE
    suspend fun addToFavourites(userId: Long, propertyId: Long): Result<Boolean>

    suspend fun storeUserAction(userId: Long, propertyId: Long, action: UserActionEnum): Result<Boolean>

    suspend fun storeInterestedProperty(userId: Long, propertyId: Long): Result<PropertyLead>

    // READ
    suspend fun getFavourites(userId: Long, pagination: Pagination): Result<List<PropertySummary>>

    suspend fun getUserActions(userId: Long): Result<List<UserActionData>>

    suspend fun getLeads(landlordId: Long, pagination: Pagination): Result<List<PropertyLead>> // Have to write join query

    // UPDATE
    suspend fun updateLead(leadId: Long, updateFields: Map<String, Any>): Result<Boolean>

    // DELETE
    suspend fun deleteFromFavourites(userId: Long, propertyId: Long): Result<Boolean>
}