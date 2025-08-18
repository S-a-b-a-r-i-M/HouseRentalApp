package com.example.houserentalapp.data.repo

import android.content.Context
import com.example.houserentalapp.data.local.db.DatabaseHelper
import com.example.houserentalapp.data.local.db.dao.PropertyDao
import com.example.houserentalapp.domain.model.Pagination
import com.example.houserentalapp.domain.model.Property
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.domain.repo.PropertyRepo
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.extensions.logError

class PropertyRepoImpl(context: Context) : PropertyRepo {
    private val propertyDao = PropertyDao(DatabaseHelper.getInstance(context))

    // -------------- CREATE --------------
    override suspend fun createProperty(property: Property): Result<Long> {
        return try {
            TODO("Not yet implemented")
        } catch (e: Exception) {
            logError("Error creating property", e)
            Result.Error(e.message.toString())
        }
    }

    // -------------- READ --------------
    override suspend fun getDetailedPropertyById(propertyId: Long): Result<Property> {
        TODO("Not yet implemented")
    }

    override suspend fun getPropertySummaries(
        filters: Map<String, Any>, pagination: Pagination
    ): Result<List<PropertySummary>> {
        TODO("Not yet implemented")
    }

    // -------------- UPDATE --------------
    override suspend fun updateProperty(
        propertyId: Long, updateFields: Map<String, Any>
    ): Result<Property> {
        TODO("Not yet implemented")
    }

    // -------------- DELETE --------------
    override suspend fun deleteProperty(propertyId: Long, userId: Long): Result<Boolean> {
        TODO("Not yet implemented")
    }
}