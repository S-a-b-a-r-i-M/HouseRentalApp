package com.example.houserentalapp.data.local.db.entity

data class UserEntity (
    val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String,
    val createdAt: Long
)
