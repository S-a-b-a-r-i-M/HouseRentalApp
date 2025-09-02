package com.example.houserentalapp.domain.model

import com.example.houserentalapp.domain.model.enums.UserActionEnum

data class UserActionData (
    val id: Long,
    val propertyId: Long,
    val action: UserActionEnum,
    val createdAt: Long
)
