package com.example.houserentalapp.domain.model

import com.example.houserentalapp.domain.model.enums.UserActionEnum

data class UserActionData (
    val propertyId: Long,
    val action: UserActionEnum
)
