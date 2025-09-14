package com.example.houserentalapp.domain.model

data class PropertyImage (
    val id: Long,
    val imageSource: ImageSource,
    val isPrimary: Boolean
)