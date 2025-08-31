package com.example.houserentalapp.data.mapper

import android.database.Cursor
import com.example.houserentalapp.data.local.db.entity.PropertyImageEntity
import com.example.houserentalapp.data.local.db.tables.PropertyImagesTable
import com.example.houserentalapp.domain.model.ImageSource
import com.example.houserentalapp.domain.model.PropertyImage

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
}