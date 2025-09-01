package com.example.houserentalapp.data.mapper

import android.database.Cursor
import com.example.houserentalapp.data.local.db.entity.PropertyImageEntity
import com.example.houserentalapp.data.local.db.tables.PropertyImagesTable
import com.example.houserentalapp.domain.model.ImageSource
import com.example.houserentalapp.domain.model.PropertyImage
import com.example.houserentalapp.presentation.utils.extensions.logError
import org.json.JSONArray

object PropertyImageMapper {
    fun fromDomain(image: PropertyImage): PropertyImageEntity {
        return PropertyImageEntity(
            id = image.id,
            imageAddress = image.imageAddress,
            isPrimary = image.isPrimary
        )
    }

    fun toDomain(entity: PropertyImageEntity): PropertyImage {
        if(entity.id == null)
            throw IllegalArgumentException("Property Image id is missing")

        return PropertyImage(
            id = entity.id,
            imageAddress = entity.imageAddress,
            imageSource = ImageSource.LocalFile(entity.imageAddress),
            isPrimary = entity.isPrimary
        )
    }

    fun toEntity(cursor: Cursor): PropertyImageEntity {
        with(cursor) {
            return PropertyImageEntity(
                id = getLong(
                    getColumnIndexOrThrow(
                        PropertyImagesTable.COLUMN_ID
                    )
                ),
                imageAddress = getString(
                    getColumnIndexOrThrow(
                        PropertyImagesTable.COLUMN_IMAGE_ADDRESS
                    )
                ),
                isPrimary = getInt(
                    getColumnIndexOrThrow(
                        PropertyImagesTable.COLUMN_IS_PRIMARY
                    )
                ) == 1,
            )
        }
    }

    fun toEntityFromJson(imagesJson: String): List<PropertyImageEntity> {
        if (imagesJson.isBlank() || imagesJson == "[]") return emptyList()

        try {
            val jsonArray = JSONArray(imagesJson)
            val imagesEntity = mutableListOf<PropertyImageEntity>()

            for (i in 0 until jsonArray.length()) {
                val imageObject = jsonArray.getJSONObject(i)

                imagesEntity.add(
                    PropertyImageEntity (
                        id = imageObject.getLong(PropertyImagesTable.COLUMN_ID),
                        imageAddress = imageObject.getString(PropertyImagesTable.COLUMN_IMAGE_ADDRESS),
                        isPrimary = imageObject.getInt(PropertyImagesTable.COLUMN_IS_PRIMARY) == 1
                    )
                )
            }
            return imagesEntity
        } catch (e: Exception) {
            logError("Error parsing images JSON: $e")
            return emptyList()
        }
    }
}