package com.example.houserentalapp.data.repo

import android.content.Context
import com.example.houserentalapp.data.local.db.DatabaseHelper
import com.example.houserentalapp.data.local.db.dao.PropertyDao
import com.example.houserentalapp.data.local.db.entity.PropertyImageEntity
import com.example.houserentalapp.data.mapper.PropertyImageMapper
import com.example.houserentalapp.data.mapper.PropertyMapper
import com.example.houserentalapp.data.util.PropertyImageStorage
import com.example.houserentalapp.domain.model.AmenityDomain
import com.example.houserentalapp.domain.model.ImageSource
import com.example.houserentalapp.domain.model.Pagination
import com.example.houserentalapp.domain.model.Property
import com.example.houserentalapp.domain.model.PropertyFilters
import com.example.houserentalapp.domain.model.PropertyImage
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.domain.model.enums.AmenityType
import com.example.houserentalapp.domain.model.enums.PropertyFields
import com.example.houserentalapp.domain.repo.PropertyRepo
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.logWarning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PropertyRepoImpl(private val context: Context) : PropertyRepo {
    private val propertyDao = PropertyDao(DatabaseHelper.getInstance(context))
    private val imageStorage : PropertyImageStorage by lazy { PropertyImageStorage(context) }

    // -------------- CREATE --------------
    override suspend fun createProperty(property: Property): Result<Long> {
        return try {
            withContext(Dispatchers.IO) {
                val imageEntityList = mutableListOf<PropertyImageEntity>()
                property.images.forEachIndexed { idx, it ->
                    if (it.imageSource is ImageSource.Uri) {
                        val fileName = imageStorage.saveImage(property.id, it.imageSource.uri)
                        if (fileName != null)
                            imageEntityList.add(
                                PropertyImageEntity(0, fileName, idx == 0)
                            )
                    }
                }

                val propertyEntity = PropertyMapper.fromDomain(property)
                val propertyId = propertyDao.insertProperty(propertyEntity.copy(images = imageEntityList))
                logDebug("Property($propertyId) created successfully.")
                Result.Success(propertyId)
            }
        } catch (e: Exception) {
            logError("Error creating property", e)
            Result.Error(e.message.toString())
        }
    }

    suspend fun createPropertyImages(
        propertyId: Long, propertyImages: List<PropertyImage>
    ): Result<List<Long?>> {
        return try {
            withContext(Dispatchers.IO) {
                if (propertyImages.isEmpty()) {
                    logWarning("createPropertyImages images is empty")
                    return@withContext Result.Success(emptyList())
                }

                val imageEntityList = mutableListOf<PropertyImageEntity>()
                propertyImages.forEachIndexed { idx, it ->
                    if (it.imageSource is ImageSource.Uri) {
                        val fileName = imageStorage.saveImage(propertyId, it.imageSource.uri)
                        if (fileName != null)
                            imageEntityList.add(
                                PropertyImageEntity(0, fileName, idx == 0)
                            )
                    }
                }

                val ids = propertyDao.insertPropertyImages(propertyId = propertyId, images = imageEntityList)
                logDebug("${ids.size} images Created For Property($propertyId) successfully.")
                Result.Success(ids)
            }
        } catch (e: Exception) {
            logError("Error updating property availability (${propertyId})", e)
            Result.Error(e.message.toString())
        }
    }

    suspend fun createPropertyAmenities(
        propertyId: Long, amenities: List<AmenityDomain>
    ): Result<Boolean> {
        return try {
            withContext(Dispatchers.IO) {
                if (amenities.isEmpty()) {
                    logWarning("createPropertyAmenities amenities is empty")
                    return@withContext Result.Success(true)
                }

                val ids = propertyDao.createAmenities(propertyId = propertyId, amenities = amenities)
                logDebug("${ids.size} Amenities created for Property($propertyId)")
                Result.Success(ids.isNotEmpty())
            }
        } catch (e: Exception) {
            logError("Error updating property availability (${propertyId})", e)
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
                    logDebug("Property($propertyId) view count update result: $updateCount")
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
        userId: Long,
        pagination: Pagination,
        filters: PropertyFilters?
    ): Result<List<Pair<PropertySummary, Boolean>>> {
        return try {
            withContext(Dispatchers.IO) {
                val summariesWithShortlistState = propertyDao.getPropertySummariesWithFilter(
                    userId, pagination, filters
                )
                logDebug("getPropertySummaries retrieved ${summariesWithShortlistState.size} summaries")

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

    suspend fun updatePropertyAmenities(
        propertyId: Long, amenities: List<AmenityDomain>
    ): Result<Boolean> {
        return try {
            withContext(Dispatchers.IO) {
                val updatedRows = propertyDao.updateAmenities(amenities)
                if (updatedRows > 0) {
                    logDebug("Property($propertyId) $updatedRows Amenities updated")
                    return@withContext Result.Success(true)
                }
                else {
                    logDebug("Failed to update Amenities for Property($propertyId)")
                    Result.Error("Failed to update property amenities")
                }
                Result.Success(true)
            }
        } catch (e: Exception) {
            logError("Error updating property availability (${propertyId})", e)
            Result.Error(e.message.toString())
        }
    }

    suspend fun handlePropertyImageChanges(propertyId: Long, images: List<PropertyImage>) {
        val toCreate = mutableListOf<PropertyImage>()
        val currentImagesIdSet = mutableSetOf<Long>()
        images.forEach {
            if (it.id == 0L) toCreate.add(it) // New Images
            else currentImagesIdSet.add(it.id)
        }

        // Create
        val existingImages = propertyDao.getPropertyImages(propertyId = propertyId) // Take Existing images before create
        if (toCreate.isNotEmpty()) {
            createPropertyImages(propertyId, toCreate)
            if (currentImagesIdSet.size == toCreate.size)
                return // No More Images To Delete
        }

        // Delete
        val toDelete = existingImages.filter { !currentImagesIdSet.contains(it.id) }
        if (toDelete.isNotEmpty())
            deletePropertyImages (
                propertyId,
                toDelete.map { PropertyImageMapper.toDomain(it) }
            )
    }

    suspend fun handleAmenitiesChanges(propertyId: Long, amenities: List<AmenityDomain>) {
        val toCreate = mutableListOf<AmenityDomain>()
        val toUpdate = mutableListOf<AmenityDomain>()
        val currentAmenitiesIdSet = mutableSetOf<Long>()
        val existingAmenitiesCountMap = propertyDao.getPropertyAmenities(
            propertyId = propertyId
        ).associate { it.id to it.count }

        amenities.forEach {
            if (it.id == 0L) toCreate.add(it) // New
            else {
                currentAmenitiesIdSet.add(it.id)
                if (
                    it.type == AmenityType.INTERNAL_COUNTABLE && // Include only IC
                    it.count != existingAmenitiesCountMap[it.id] // Include only if count changed
                )
                    toUpdate.add(it)
            }
        }

        // Create
        if (toCreate.isNotEmpty())
            createPropertyAmenities(propertyId, toCreate)

        // Delete
        val toDeleteIds = existingAmenitiesCountMap.keys - currentAmenitiesIdSet
        if (toDeleteIds.isNotEmpty())
            deletePropertyAmenities(propertyId, toDeleteIds.toList())

        // Update
        if (toUpdate.isNotEmpty())
            updatePropertyAmenities(propertyId, toUpdate)
    }

    override suspend fun updateProperty(
        property: Property, updatedFields: List<PropertyFields>
    ): Result<Boolean> {
        return try {
            withContext(Dispatchers.IO) {
                // Update Property Basic Details Changes
                propertyDao.updateProperty(property, updatedFields)

                // Handle Property Images Changes
                if (PropertyFields.IMAGES in updatedFields)
                    handlePropertyImageChanges(property.id, property.images)

                // Handle Property Amenities Changes
                if (PropertyFields.AMENITIES in updatedFields)
                    handleAmenitiesChanges(property.id, property.amenities)

                logDebug("Property(${property.id}) updated")
                Result.Success(true)
            }
        } catch (e: Exception) {
            logError("Error updating property availability (${property.id})", e)
            Result.Error(e.message.toString())
        }
    }

    override suspend fun updatePropertyAvailability(
        propertyId: Long, isAvailable: Boolean
    ): Result<Boolean> {
        return try {
            withContext(Dispatchers.IO) {
                val updatedRows = propertyDao.updatePropertyAvailability(propertyId, isAvailable)
                if (updatedRows > 0) {
                    logDebug("Property($propertyId) availability changed Available: $isAvailable")
                    return@withContext Result.Success(true)
                }
                else {
                    logDebug("Failed to update property availability: $isAvailable")
                    Result.Error("Failed to update property availability($propertyId)")
                }
            }
        } catch (e: Exception) {
            logError("Error updating property availability ($propertyId)", e)
            Result.Error(e.message.toString())
        }
    }

    // -------------- DELETE --------------
    override suspend fun deleteProperty(propertyId: Long, userId: Long): Result<Boolean> {
        return try {
            withContext(Dispatchers.IO) {
                val updatedRows = propertyDao.deleteProperty(propertyId, userId)
                if (updatedRows > 0) {
                    imageStorage.deleteAllImagesByProperty(propertyId) // Delete Images
                    logDebug("Property($propertyId) deleted successfully.")
                    return@withContext Result.Success(true)
                }
                else {
                    logDebug("Failed to delete property($propertyId)")
                    Result.Error("Failed to delete property")
                }
            }
        } catch (e: Exception) {
            logError("Error while deleting property($propertyId)", e)
            Result.Error(e.message.toString())
        }
    }

    suspend fun deletePropertyImages(
        propertyId: Long, propertyImages: List<PropertyImage>
    ): Result<Boolean> {
        return try {
            withContext(Dispatchers.IO) {
                val deletedRows = propertyDao.deletePropertyImages(propertyImages.map { it.id })
                if (deletedRows > 0) {
                    logDebug("Property($propertyId):${deletedRows} images deleted successfully from db.")
                    propertyImages.forEach {
                        if (it.imageSource is ImageSource.LocalFile)
                            imageStorage.deleteImageByPath(it.imageSource.filePath)
                    }
                    logDebug("Property($propertyId) images deleted successfully from internal storage.")
                    return@withContext Result.Success(true)
                }
                else {
                    logDebug("Failed to delete property($propertyId) from db")
                    Result.Error("Failed to delete property images")
                }
            }
        } catch (e: Exception) {
            logError("Error updating property availability (${propertyId})", e)
            Result.Error(e.message.toString())
        }
    }

    suspend fun deletePropertyAmenities(propertyId: Long, amenityIds: List<Long>): Result<Boolean> {
        return try {
            withContext(Dispatchers.IO) {
                val deletedRows = propertyDao.deleteAmenities(propertyId, amenityIds)
                logDebug("Property($propertyId):${deletedRows} images deleted from db.")
                Result.Success(deletedRows > 0)
            }
        } catch (e: Exception) {
            logError("Error updating property availability (${propertyId})", e)
            Result.Error(e.message.toString())
        }
    }
}