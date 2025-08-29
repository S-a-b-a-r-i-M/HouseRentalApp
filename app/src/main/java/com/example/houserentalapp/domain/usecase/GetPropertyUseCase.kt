package com.example.houserentalapp.domain.usecase

import com.example.houserentalapp.domain.model.Pagination
import com.example.houserentalapp.domain.model.Property
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.domain.repo.PropertyRepo
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.extensions.logError

class GetPropertyUseCase(private val propertyRepo: PropertyRepo) {
    suspend fun getPropertySummaries(
        filters: Map<String, Any>, pagination: Pagination
    ): Result<List<PropertySummary>> {
        return try {
            return propertyRepo.getPropertySummaries(filters, pagination)
        } catch (exp: Exception) {
            logError("${exp.message.toString()} while fetching property summaries")
            Result.Error(exp.message.toString())
        }
    }

    suspend fun getProperty(propertyId: Long): Result<Property> {
        return try {
            return propertyRepo.getDetailedPropertyById(propertyId)
        } catch (exp: Exception) {
            logError("${exp.message.toString()} while fetching property(id: $propertyId)")
            Result.Error(exp.message.toString())
        }
    }
}