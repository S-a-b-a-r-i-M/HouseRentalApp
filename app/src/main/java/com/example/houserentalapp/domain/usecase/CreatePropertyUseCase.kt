package com.example.houserentalapp.domain.usecase

import com.example.houserentalapp.domain.model.Property
import com.example.houserentalapp.domain.repo.PropertyRepo
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo

class CreatePropertyUseCase(private val propertyRepo: PropertyRepo) {
    suspend operator fun invoke(property: Property): Result<Long> { // invoke() is Kotlin's way to make objects "callable" like functions
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
}