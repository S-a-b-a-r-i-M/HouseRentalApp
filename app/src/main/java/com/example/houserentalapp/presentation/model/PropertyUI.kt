package com.example.houserentalapp.presentation.model

import com.example.houserentalapp.domain.model.Property
import com.example.houserentalapp.domain.model.User

data class PropertyUI (
    val property: Property,
    val isShortlisted: Boolean = false,
    val isInterested: Boolean = false,
    val landlordUser: User? = null,
    // Flags
    var propertyInfoChanged: Boolean = false,
    var shortlistStateChanged: Boolean = false,
    var interestedStateChanged: Boolean = false,
    var landlordUserInfoChanged: Boolean = false,
) {
    fun resetFlags() {
        propertyInfoChanged = false
        shortlistStateChanged = false
        interestedStateChanged = false
        landlordUserInfoChanged = false
    }
}