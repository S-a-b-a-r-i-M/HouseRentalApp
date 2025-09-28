package com.example.houserentalapp.data.local.db.entity

import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.model.enums.LeadStatus

data class LeadEntity(
    val id: Long,
    val lead: User,
    val interestedPropertyIdsWithStatus: List<Pair<Long, LeadStatus>>,
    val note: String? = null,
    val createdAt: Long,
)