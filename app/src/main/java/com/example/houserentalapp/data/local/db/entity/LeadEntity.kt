package com.example.houserentalapp.data.local.db.entity

import com.example.houserentalapp.domain.model.User

data class LeadEntity(
    val id: Long,
    val lead: User,
    val interestedPropertyIds: List<Long>,
    val status: String,
    val note: String? = null,
    val createdAt: Long,
)