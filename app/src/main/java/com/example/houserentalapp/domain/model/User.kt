package com.example.houserentalapp.domain.model

data class User (
    val id: Long,
    val name: String,
    val email: String,
    val phone: String,
    val hashedPassword: String,
    val createdAt: Int
)