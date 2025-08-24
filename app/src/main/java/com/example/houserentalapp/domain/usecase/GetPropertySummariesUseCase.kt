package com.example.houserentalapp.domain.usecase

import com.example.houserentalapp.domain.model.Pagination
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.domain.repo.PropertyRepo
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo

class GetPropertySummariesUseCase(private val propertyRepo: PropertyRepo) {
    suspend operator fun invoke(filters: Map<String, Any>, pagination: Pagination): Result<List<PropertySummary>> {
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
}