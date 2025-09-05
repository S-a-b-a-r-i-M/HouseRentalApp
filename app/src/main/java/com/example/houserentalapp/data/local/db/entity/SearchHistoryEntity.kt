package com.example.houserentalapp.data.local.db.entity

data class SearchHistoryEntity(
    val userId: Long,
    val query: String,
    val meta: String, // Json string
    val createdAt: Long = System.currentTimeMillis()
)