package com.example.houserentalapp.domain.usecase

import com.example.houserentalapp.domain.model.Pagination
import com.example.houserentalapp.domain.model.Property
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.domain.repo.PropertyRepo
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo

class GetPropertyUseCase(private val propertyRepo: PropertyRepo) {
    suspend fun getPropertySummaries(
        filters: Map<String, Any>, pagination: Pagination
    ): Result<List<PropertySummary>> {
        return try {
            when(val result = propertyRepo.getPropertySummaries(filters, pagination)) {
                is Result.Success<List<PropertySummary>> -> {
                    logInfo("${result.data.size} Property summaries fetched with ${filters.size} filters")
                    result
                }
                is Result.Error -> {
                    logError("Error: $result while fetching property summaries")
                    result
                }
            }
        } catch (exp: Exception) {
            logError("${exp.message.toString()} while fetching property summaries")
            Result.Error(exp.message.toString())
        }
    }

    suspend fun getProperty(propertyId: Long): Result<Property> {
        return try {
            when(val result = propertyRepo.getDetailedPropertyById(propertyId)) {
                is Result.Success<Property> -> {
                    logInfo("${result.data.name} Property(id: $propertyId) fetched successfully")
                    result
                }
                is Result.Error -> {
                    logError("Error: $result while fetching property(id: $propertyId)")
                    result
                }
            }
        } catch (exp: Exception) {
            logError("${exp.message.toString()} while fetching property(id: $propertyId)")
            Result.Error(exp.message.toString())
        }
    }
}