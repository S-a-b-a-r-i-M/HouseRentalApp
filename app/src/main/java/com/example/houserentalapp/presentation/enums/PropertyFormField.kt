package com.example.houserentalapp.presentation.enums

enum class PropertyFormField(val isRequired: Boolean = true) {
    CITY,
    LOCALITY,
    STREET,
    TYPE,
    BHK,
    NAME,
    DESCRIPTION(false),
    KIND,
    FURNISHING_TYPE,
    BATH_ROOM_COUNT(false),
    COVERED_PARKING_COUNT(false),
    OPEN_PARKING_COUNT(false),
    IS_PET_FRIENDLY,
    PREFERRED_TENANT_TYPE,
    PREFERRED_BACHELOR_TYPE,
    BUILT_UP_AREA,
    AVAILABLE_FROM,
    PRICE,
    IS_MAINTENANCE_SEPARATE,
    MAINTENANCE_CHARGES,
    SECURITY_DEPOSIT,
    IMAGES
}