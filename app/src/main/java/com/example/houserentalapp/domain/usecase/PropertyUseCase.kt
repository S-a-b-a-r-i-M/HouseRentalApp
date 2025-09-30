package com.example.houserentalapp.domain.usecase

import com.example.houserentalapp.domain.model.Pagination
import com.example.houserentalapp.domain.model.Property
import com.example.houserentalapp.domain.model.PropertyFilters
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.domain.model.enums.PropertyFields
import com.example.houserentalapp.domain.repo.PropertyRepo
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo

class PropertyUseCase(private val propertyRepo: PropertyRepo) {
    suspend fun getPropertySummaries(
        userId: Long,
        pagination: Pagination,
        filters: PropertyFilters? = null
    ): Result<List<Pair<PropertySummary, Boolean>>> {
        return try {
            return propertyRepo.getPropertySummaries(userId, pagination, filters)
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

    suspend fun getPropertySummary(userId: Long, propertyId: Long): Result<PropertySummary> {
        return try {
            return propertyRepo.getPropertySummary(userId, propertyId)
        } catch (exp: Exception) {
            logError("${exp.message.toString()} while fetching property(id: $propertyId)")
            Result.Error(exp.message.toString())
        }
    }

    suspend fun createProperty(property: Property): Result<Long> {
        return try {
            when(val result = propertyRepo.createProperty(property)){
                is Result.Success<Long> -> {
                    logInfo("Property(${property.name}) created successfully with id: ${result.data}")
                    result
                }
                is Result.Error -> {
                    logError("Property Creation failed")
                    result
                }
            }
        } catch (exp: Exception) {
            Result.Error("error")
        }
    }

    suspend fun updateProperty(property: Property, updatedFields: List<PropertyFields>): Result<Boolean> {
        return try {
            when(val result = propertyRepo.updateProperty(property, updatedFields)){
                is Result.Success<Boolean> -> {
                    logInfo("Property(${property.name}) updated successfully with id: ${property.id}")
                    result
                }
                is Result.Error -> {
                    logError("Property Creation failed")
                    result
                }
            }
        } catch (exp: Exception) {
            Result.Error("error")
        }
    }

    suspend fun updatePropertyAvailability(propertyId: Long, isAvailable: Boolean): Result<Boolean> {
        return try {
            when(val result = propertyRepo.updatePropertyAvailability(propertyId, isAvailable)){
                is Result.Success<Boolean> -> {
                    logInfo("Property(${propertyId}) availability changed successfully")
                    result
                }
                is Result.Error -> {
                    logError("Property availability change failed")
                    result
                }
            }
        } catch (exp: Exception) {
            Result.Error("error")
        }
    }

    suspend fun deleteProperty(propertyId: Long, userId: Long): Result<Boolean> {
        return try {
            when(val result = propertyRepo.deleteProperty(propertyId, userId)){
                is Result.Success<Boolean> -> {
                    logInfo("Property(${propertyId}) deleted successfully")
                    result
                }
                is Result.Error -> {
                    logError("Property delete action failed")
                    result
                }
            }
        } catch (exp: Exception) {
            Result.Error("error")
        }
    }
}