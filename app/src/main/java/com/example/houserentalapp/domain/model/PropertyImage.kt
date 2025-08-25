package com.example.houserentalapp.domain.model

data class PropertyImage (
    val id: Long?,
    val imageAddress: String,
    val imageSource: ImageSource? = null, // Check the logic
    val isPrimary: Boolean
)

sealed class ImageSource() {
    class LocalFile(val fileAddress: String) : ImageSource()
    class Uri(val uri: android.net.Uri) : ImageSource()
}