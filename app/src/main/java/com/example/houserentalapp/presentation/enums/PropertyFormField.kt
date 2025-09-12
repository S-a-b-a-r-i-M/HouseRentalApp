package com.example.houserentalapp.presentation.enums

enum class PropertyFormField(val isRequired: Boolean = true) {
    NAME,
    DESCRIPTION(false),
    KIND,
    TYPE,
    FURNISHING_TYPE,
    PREFERRED_TENANT_TYPE,
    PREFERRED_BACHELOR_TYPE,
    BATH_ROOM_COUNT(false),
    COVERED_PARKING_COUNT(false),
    OPEN_PARKING_COUNT(false),
    AVAILABLE_FROM,
    BHK,
    BUILT_UP_AREA,
    IS_PET_FRIENDLY,
    PRICE,
    IS_MAINTENANCE_SEPARATE,
    MAINTENANCE_CHARGES,
    SECURITY_DEPOSIT,
    STREET,
    LOCALITY,
    CITY,
    IMAGES
}