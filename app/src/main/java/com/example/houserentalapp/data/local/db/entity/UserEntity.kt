package com.example.houserentalapp.data.local.db.entity

data class UserEntity (
    val id: Long,
    val name: String,
    val phone: String,
    val email: String,
    val password: String,
    val createdAt: Long
)
