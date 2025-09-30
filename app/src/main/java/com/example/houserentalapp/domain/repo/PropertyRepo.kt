package com.example.houserentalapp.domain.repo

import com.example.houserentalapp.domain.model.Pagination
import com.example.houserentalapp.domain.model.Property
import com.example.houserentalapp.domain.model.PropertyFilters
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.domain.model.enums.PropertyFields
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

    suspend fun getPropertySummary(userId: Long, propertyId: Long): Result<PropertySummary>

    // UPDATE
    suspend fun updateProperty(property: Property, updatedFields: List<PropertyFields> = emptyList()): Result<Boolean>

    suspend fun updatePropertyAvailability(propertyId: Long, isAvailable: Boolean): Result<Boolean>

    // DELETE
    suspend fun deleteProperty(propertyId: Long, userId: Long): Result<Boolean>
}