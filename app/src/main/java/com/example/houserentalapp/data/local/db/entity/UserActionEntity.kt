package com.example.houserentalapp.data.local.db.entity

data class UserActionEntity (
    val id: Long,
    val propertyId: Long,
    val action: String,
    val createdAt: Long
)