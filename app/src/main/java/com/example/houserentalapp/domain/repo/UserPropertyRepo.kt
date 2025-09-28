package com.example.houserentalapp.domain.repo

import com.example.houserentalapp.domain.model.Pagination
import com.example.houserentalapp.domain.model.Lead
import com.example.houserentalapp.domain.model.UserActionData
import com.example.houserentalapp.domain.model.UserPropertyStats
import com.example.houserentalapp.domain.model.enums.LeadStatus
import com.example.houserentalapp.domain.model.enums.LeadUpdatableField
import com.example.houserentalapp.domain.model.enums.UserActionEnum
import com.example.houserentalapp.domain.utils.Result

interface UserPropertyRepo {
    // CREATE
    suspend fun storeUserAction(userId: Long, propertyId: Long, action: UserActionEnum): Result<Long>

    suspend fun createLead(
        tenantId: Long,
        landlordId: Long,
        propertyId: Long,
        status: LeadStatus
    ): Result<Long>

    // READ
    suspend fun getPropertyWithUserActions(tenantId: Long, propertyId: Long) : Result<Map<String, Any>>

    suspend fun getUserActions(userId: Long, propertyIds: List<Long>): Result<List<UserActionData>>

    suspend fun getLeadsByLandlord(landlordId: Long, pagination: Pagination): Result<List<Lead>>

    suspend fun getUserPropertyStats(userId: Long): Result<UserPropertyStats>

    // UPDATE
    suspend fun updateLeadNote(leadId: Long, newNotes: String): Result<Boolean>

    suspend fun updateLeadPropertyStatus(
        leadId: Long, propertyId: Long, newStatus: LeadStatus
    ): Result<Boolean>

        // DELETE
    suspend fun deleteUserAction(userId: Long, propertyId: Long, action: UserActionEnum): Result<Boolean>
}