package com.example.houserentalapp.domain.repo

import com.example.houserentalapp.domain.model.Pagination
import com.example.houserentalapp.domain.model.Property
import com.example.houserentalapp.domain.model.PropertyFilters
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.domain.utils.Result

interface PropertyRepo {

    // CREATE
    suspend fun createProperty(property: Property): Result<Long>

    // READ
    suspend fun getDetailedPropertyById(propertyId: Long): Result<Property>

    suspend fun getPropertySummaries(
        userId: Long,
        pagination: Pagination,
        filters: PropertyFilters? = null
    ): Result<List<Pair<PropertySummary, Boolean>>>

    // UPDATE
    suspend fun updateProperty(propertyId: Long, updateFields: Map<String, Any>): Result<Property>

    // DELETE
    suspend fun deleteProperty(propertyId: Long, userId: Long): Result<Boolean>
}