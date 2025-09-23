package com.example.houserentalapp.domain.model

data class UserPropertyStats (
    val viewedPropertyCount: Int,
    val shortlistedPropertyCount: Int,
    val contactViewedPropertyCount: Int,
    val listedPropertyCount: Int,
    val leadsCount: Int
)
