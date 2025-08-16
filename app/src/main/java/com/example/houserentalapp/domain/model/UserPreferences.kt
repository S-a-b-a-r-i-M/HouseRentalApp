package com.example.houserentalapp.domain.model

import com.example.houserentalapp.domain.model.enums.BHK
import com.example.houserentalapp.domain.model.enums.LookingTo

data class UserPreferences(
    val city: String?,
    val bhk: BHK?,
    val lookingTo: LookingTo?
)
