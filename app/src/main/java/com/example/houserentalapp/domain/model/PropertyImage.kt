package com.example.houserentalapp.domain.model

data class PropertyImage (
    val id: Long,
    val imageSource: ImageSource,
    val isPrimary: Boolean
)

sealed class ImageSource() {
    class LocalFile(val filePath: String) : ImageSource()
    class Uri(val uri: android.net.Uri) : ImageSource()
}