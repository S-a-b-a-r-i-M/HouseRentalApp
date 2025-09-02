package com.example.houserentalapp.presentation.model

import com.example.houserentalapp.domain.model.Property
import com.example.houserentalapp.domain.model.UserActionData

data class PropertyWithActionsUI (
    val property: Property,
    val userActionDataList: List<UserActionData>? = null
)