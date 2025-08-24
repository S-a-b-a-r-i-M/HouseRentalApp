package com.example.houserentalapp.data.repo

import android.content.Context
import android.util.Log
import com.example.houserentalapp.data.local.db.DatabaseHelper
import com.example.houserentalapp.data.local.db.dao.PropertyDao
import com.example.houserentalapp.data.mapper.PropertyMapper
import com.example.houserentalapp.domain.model.Pagination
import com.example.houserentalapp.domain.model.Property
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.domain.repo.PropertyRepo
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PropertyRepoImpl(context: Context) : PropertyRepo {
    private val propertyDao = PropertyDao(DatabaseHelper.getInstance(context))

    // -------------- CREATE --------------
    override suspend fun createProperty(property: Property): Result<Long> {
        return try {
            withContext(Dispatchers.IO) {
                val propertyEntity = PropertyMapper.fromDomain(property)
                val propertyId = propertyDao.insertProperty(propertyEntity)
                logInfo("Property($propertyId) created successfully.")
                Result.Success(propertyId)
            }
        } catch (e: Exception) {
            logError("Error creating property", e)
            Result.Error(e.message.toString())
        }
    }

    // -------------- READ --------------
    override suspend fun getDetailedPropertyById(propertyId: Long): Result<Property> {
        return try {
            withContext(Dispatchers.IO) {
                val propertyEntity = propertyDao.getPropertyById(propertyId)
                if (propertyEntity != null) {
                    val propertyDomain = PropertyMapper.toDomain(propertyEntity)
                    // Update Views count
                    val updateCount = propertyDao.incrementViewCount(propertyId)
                    logInfo("Property($propertyId) view count update result: $updateCount")
                    Result.Success(propertyDomain)
                } else
                    Result.Error("Property not found for id: $propertyId")
            }
        } catch (e: Exception) {
            logError("Error reading property (id: $propertyId)", e)
            Result.Error(e.message.toString())
        }
    }

    override suspend fun getPropertySummaries(
        filters: Map<String, Any>, pagination: Pagination
    ): Result<List<PropertySummary>> {
        return try {
            withContext(Dispatchers.IO) {
                val (summariesEntity, count) = propertyDao.getPropertySummariesWithFilter(
                    filters, pagination
                )
                val summariesDomain = summariesEntity.map { PropertyMapper.toPropertySummary(it) }
                logInfo("getPropertySummaries retrieved ${summariesDomain.size} summaries")
                Result.Success(summariesDomain, meta = mapOf("total_records" to count))
            }
        } catch (e: Exception) {
            logError("Error reading property summaries", e)
            Result.Error(e.message.toString())
        }
    }

    // -------------- UPDATE --------------
    override suspend fun updateProperty(
        propertyId: Long, updateFields: Map<String, Any>
    ): Result<Property> {
        return try {
            withContext(Dispatchers.IO) {
                val updatedRows = propertyDao.updateProperty(propertyId, updateFields)
                if (updatedRows > 0) {
                    // Read Property
                    val propertyEntity = propertyDao.getPropertyById(propertyId)
                    if(propertyEntity != null)
                        return@withContext Result.Success(PropertyMapper.toDomain(propertyEntity))

                    Log.wtf(
                        "PropertyRepoImpl",
                        "property($propertyId) not found after update"
                    )
                    Result.Error("Property($propertyId) not found after update")
                } else
                    Result.Error("Failed to update property($propertyId)")
            }
        } catch (e: Exception) {
            logError("Error updating property ($propertyId)", e)
            Result.Error(e.message.toString())
        }
    }

    // -------------- DELETE --------------
    override suspend fun deleteProperty(propertyId: Long, userId: Long): Result<Boolean> {
        TODO("Not yet implemented")
    }
}