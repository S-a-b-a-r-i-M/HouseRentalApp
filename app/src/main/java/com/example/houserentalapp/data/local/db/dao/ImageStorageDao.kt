package com.example.houserentalapp.data.local.db.dao

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.extensions.logError
import java.io.File

class ImageStorageDao(private val context: Context) {
    // SAVE INTO INTERNAL STORAGE
    fun savePropertyImage(propertyId: Long, imageUri: Uri): String? {
        val imagesDir = File(context.filesDir, PROPERTY_IMAGES_DIR)
        if (!imagesDir.exists() && !imagesDir.mkdir()) {
            logError("Failed to create images directory")
            return null
        }

        val propertyImagesDir = File(imagesDir, "$propertyId")
        if (!propertyImagesDir.exists() && !propertyImagesDir.mkdir()) {
            logError("Failed to create property images directory")
            return null
        }

        return saveToInternalStorage(imageUri, propertyImagesDir)
    }

    fun saveUserImage(userId: Long, imageUri: Uri): String? {
        val imagesDir = File(context.filesDir, USER_IMAGER_DIR)
        if (!imagesDir.exists() && !imagesDir.mkdir()) {
            logError("Failed to create $USER_IMAGER_DIR")
            return null
        }

        return saveToInternalStorage(imageUri, imagesDir)
    }

    private fun saveToInternalStorage(imageUri: Uri, imagesDir: File): String? {
        // Construct File Name
        val fileName = context.contentResolver.query(
            imageUri, null, null, null
        )?.use {
            it.moveToFirst()
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            "${System.currentTimeMillis()}_${it.getString(nameIndex)}"
        } ?: "${System.currentTimeMillis()}"

        // Write to destination file from imageUri
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val destinationFile = File(imagesDir, fileName)
        inputStream?.use { input ->
            destinationFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: return null

        logDebug("Image saved to internal storage. path: ${destinationFile.absolutePath}")
        return destinationFile.absolutePath // Return full path
    }

    // DELETE SINGLE IMAGE
    fun deleteImageByPath(fileAbsolutePath: String): Boolean {
        return try {
            val file = File(fileAbsolutePath)
            when {
                !file.exists() -> {
                    logDebug("File does not exist: $fileAbsolutePath")
                    return false
                }
                !file.isFile -> {
                    logError("Path is not a file: $fileAbsolutePath")
                    return false
                }
                !file.canWrite() -> {
                    logError("No write permission for file: $fileAbsolutePath")
                    return false
                }
            }

            val fileSize = file.length()
            val deleted = file.delete()

            if (deleted) {
                logDebug("Deleted image: $fileAbsolutePath (Size: ${fileSize} bytes)")
                true
            } else {
                logError("Failed to delete image: $fileAbsolutePath")
                false
            }
        } catch (e: Exception) {
            logError("Error deleting image: $fileAbsolutePath", e)
            false
        }
    }

    // DELETE WHOLE DIRECTORY OF A PROPERTY
    fun deleteAllImagesByProperty(propertyId: Long): Boolean {
        return try {
            val propertyImagesDir = File(context.filesDir, "$PROPERTY_IMAGES_DIR/$propertyId")
            if (propertyImagesDir.exists() && propertyImagesDir.isDirectory) {
                propertyImagesDir.deleteRecursively().also { success ->
                    if (success)
                        logDebug("Deleted all images for property:$propertyId")
                    else
                        logError("Failed to delete images directory for property:$propertyId")
                }
            } else {
                logDebug("No directory found for property $propertyId")
                false
            }
        } catch (e: Exception) {
            logError("Error deleting property images for $propertyId", e)
            false
        }
    }

    companion object {
        const val PROPERTY_IMAGES_DIR = "property_images"
        const val USER_IMAGER_DIR = "user_images"
    }
}