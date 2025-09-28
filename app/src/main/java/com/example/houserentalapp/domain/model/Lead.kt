package com.example.houserentalapp.domain.model

import com.example.houserentalapp.domain.model.enums.LeadStatus

data class Lead (
    val id: Long,
    val leadUser: User,
    val interestedPropertiesWithStatus: List<Pair<PropertySummary, LeadStatus>>,
    val note: String?,
    val createdAt: Long
)
