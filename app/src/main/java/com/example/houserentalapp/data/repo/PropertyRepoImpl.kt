package com.example.houserentalapp.data.repo

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.houserentalapp.data.local.db.DatabaseHelper
import com.example.houserentalapp.data.local.db.dao.PropertyDao
import com.example.houserentalapp.data.local.db.dao.UserPropertyDao
import com.example.houserentalapp.data.local.db.entity.PropertyImageEntity
import com.example.houserentalapp.data.mapper.PropertyMapper
import com.example.houserentalapp.domain.model.ImageSource
import com.example.houserentalapp.domain.model.Pagination
import com.example.houserentalapp.domain.model.Property
import com.example.houserentalapp.domain.model.PropertyFilters
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.domain.repo.PropertyRepo
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PropertyRepoImpl(private val context: Context) : PropertyRepo {
    private val dbHelper = DatabaseHelper.getInstance(context)
    private val propertyDao = PropertyDao(DatabaseHelper.getInstance(context))
    private val userPropertyDao = UserPropertyDao(dbHelper)

    // -------------- CREATE --------------
    // SAVE INTO INTERNAL STORAGE
    // TODO: Check the logic, it can be moved to use case ?
    private fun saveImageToInternalStorage(imageUri: Uri): String? {
        with(context) {
            val fileName = contentResolver.query(imageUri, null, null, null)?.use {
                it.moveToFirst()
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                "${System.currentTimeMillis()}_${it.getString((nameIndex))}"
            } ?: "${System.currentTimeMillis()}"

            val inputStream = contentResolver.openInputStream(imageUri)
            val destinationFile = File(filesDir, fileName)

            inputStream?.use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            return fileName
        }
    }

    override suspend fun createProperty(property: Property): Result<Long> {
        return try {
            withContext(Dispatchers.IO) {
                val imageEntityList = mutableListOf<PropertyImageEntity>()
                property.images.forEachIndexed { idx, it ->
                    if (it.imageSource is ImageSource.Uri) {
                        val fileName = saveImageToInternalStorage(it.imageSource.uri)
                        if (fileName != null)
                            imageEntityList.add(
                                PropertyImageEntity(null, fileName, idx == 0)
                            )
                    }
                }

                val propertyEntity = PropertyMapper.fromDomain(property)
                val propertyId = propertyDao.insertProperty(propertyEntity.copy(images = imageEntityList))
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
    /*
    override suspend fun getPropertySummaries(
        userId: Long,
        filters: Map<String, Any>,
        pagination: Pagination
    ): Result<List<Pair<PropertySummary, Boolean>>> {
        return try {
            withContext(Dispatchers.IO) {
                val (summariesEntity, _) = propertyDao.getPropertySummariesWithFilter(
                    filters, pagination
                )
                logInfo("getPropertySummaries retrieved ${summariesEntity.size} summaries")

                // Get Property Shortlisted State
                val shortlistedMap = userPropertyDao.getPropertiesShortlistedState(
                    userId, summariesEntity.map { it.id }
                )
                logInfo("${shortlistedMap.size} Property Shortlisted State retrieved")

                val resultList = summariesEntity.map {
                    val domain = PropertyMapper.toPropertySummaryDomain(it)
                    val isShortlisted = shortlistedMap.getValue(it.id)
                    Pair(domain, isShortlisted)
                }

                Result.Success(resultList)
            }
        } catch (e: Exception) {
            logError("Error reading property summaries", e)
            Result.Error(e.message.toString())
        }
    }
    */

    override suspend fun getPropertySummaries(
        userId: Long,
        pagination: Pagination,
        filters: PropertyFilters?
    ): Result<List<Pair<PropertySummary, Boolean>>> {
        return try {
            withContext(Dispatchers.IO) {
                val summariesWithShortlistState = propertyDao.getPropertySummariesWithFilter(
                    userId, pagination, filters
                )
                logInfo("getPropertySummaries retrieved ${summariesWithShortlistState.size} summaries")

                val resultList = summariesWithShortlistState.map { (entity, isShortlisted) ->
                    val domain = PropertyMapper.toPropertySummaryDomain(entity)
                    Pair(domain, isShortlisted)
                }

                Result.Success(resultList)
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