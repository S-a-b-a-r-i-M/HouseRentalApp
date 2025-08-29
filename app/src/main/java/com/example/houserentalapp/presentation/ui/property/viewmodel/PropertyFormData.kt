package com.example.houserentalapp.presentation.ui.property.viewmodel

enum class PropertyFormField {
    NAME,
    DESCRIPTION,
    LOOKING_TO,
    KIND,
    TYPE,
    FURNISHING_TYPE,
    AMENITIES,
    PREFERRED_TENANT_TYPE,
    PREFERRED_BACHELOR_TYPE,
    TRANSACTION_TYPE,
    AGE_OF_PROPERTY,
    COVERED_PARKING_COUNT,
    OPEN_PARKING_COUNT,
    AVAILABLE_FROM,
    BHK,
    BUILT_UP_AREA,
    BATH_ROOM_COUNT,
    IS_PET_FRIENDLY,
    PRICE,
    IS_MAINTENANCE_SEPARATE,
    MAINTENANCE_CHARGES,
    SECURITY_DEPOSIT,
    STREET,
    LOCALITY,
    CITY
}

data class NewPropertyImageModel (
    val imageAddress: String? = null,
    val isPrimary: Boolean? = null
)