package com.example.houserentalapp.domain.model

import com.example.houserentalapp.domain.model.enums.LeadStatus

data class PropertyLead(
    val lead: User,
    val interestedProperties: List<PropertySummary>,
    val status: LeadStatus,
    val note: String?,
    val createdAt: Long
)
