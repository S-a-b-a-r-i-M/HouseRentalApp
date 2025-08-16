package com.example.houserentalapp.data.local.db.entity

data class UserPreferenceEntity(
    val userId: Long,
    val city: String?,
    val lookingTo: String?,
    val bhk: String?
)