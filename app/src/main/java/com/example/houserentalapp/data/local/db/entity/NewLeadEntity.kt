package com.example.houserentalapp.data.local.db.entity

data class NewLeadEntity (
    val tenantId: Long,
    val landlordId: Long,
    val propertyId: Long,
    val status: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)