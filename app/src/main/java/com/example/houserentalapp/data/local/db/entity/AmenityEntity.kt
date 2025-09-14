package com.example.houserentalapp.data.local.db.entity

data class AmenityEntity(
    var id: Long = 0,
    val name: String,
    val type: String,
    val count: Int? = null
)