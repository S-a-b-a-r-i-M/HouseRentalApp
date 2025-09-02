package com.example.houserentalapp.presentation.model

import com.example.houserentalapp.domain.model.PropertySummary

data class PropertySummaryUI (
    val summary: PropertySummary,
    val isShortListed: Boolean
)
