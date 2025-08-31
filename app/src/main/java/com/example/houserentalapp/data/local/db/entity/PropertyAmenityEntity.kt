package com.example.houserentalapp.data.local.db.entity

data class PropertyAmenityEntity(
    var id: Long? = null,
    val name: String,
    val type: String,
    val count: Int? = null
)