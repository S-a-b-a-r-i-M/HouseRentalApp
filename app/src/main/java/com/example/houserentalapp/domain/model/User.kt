package com.example.houserentalapp.domain.model

data class User (
    val id: Long,
    val name: String,
    val phone: String,
    val email: String,
    val createdAt: Long
)