package com.example.houserentalapp.domain.model.enums

enum class PropertyFields(override val readable: String) : ReadableEnum {
    NAME("Name"),
    DESCRIPTION("Description"),
    KIND("Kind"),
    TYPE("Type"),
    FURNISHING_TYPE("Furnishing Type"),
    PREFERRED_TENANT_TYPE("Preferred Tenant Type"),
    PREFERRED_BACHELOR_TYPE("Preferred Bachelor Type"),
    BATH_ROOM_COUNT("Bathroom Count"),
    COVERED_PARKING_COUNT("Covered Parking Count"),
    OPEN_PARKING_COUNT("Open Parking Count"),
    AVAILABLE_FROM("Available From"),
    BHK("BHK"),
    BUILT_UP_AREA("Built-up Area"),
    IS_PET_FRIENDLY("Pet Friendly"),
    PRICE("Price"),
    IS_MAINTENANCE_SEPARATE("Maintenance Separate"),
    MAINTENANCE_CHARGES("Maintenance Charges"),
    SECURITY_DEPOSIT("Security Deposit"),
    CITY("City"),
    STREET("Street"),
    LOCALITY("Locality"),
    AMENITIES("Amenities"),
    IMAGES("Images")
}
