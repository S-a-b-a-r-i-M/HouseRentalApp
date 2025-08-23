package com.example.houserentalapp.domain.model.enums

enum class SocialAmenity(val readable: String) {
    POWER_BACKUP("Power Backup"),
    SWIMMING_POOL("Swimming Pool"),
    GYM("Gym"),
    LIFT("Lift"),
    PLAY_AREA("Play Area"),
    CCTV("CCTV"),
    GATED_COMMUNITY("Gated Community"),
    COMMUNITY_HALL("Community Hall"),
    REGULAR_WATER_SUPPLY("Regular Water Supply"),
}

enum class InternalAmenity(val readable: String) {
    WIFI("WiFi"),
    SOFA("Sofa"),
    FRIDGE("Fridge"),
    CHIMNEY("Chimney"),
    MICROWAVE("Microwave"),
    WASHING_MACHINE("Washing Machine"),
    WATER_PURIFIER("Water Purifier"),
    WATER_HEATER("Water Heater"),
}


enum class CountableInternalAmenity(val readable: String) {
    AC("AC"),
    TV("TV"),
    BED("Bed"),
    FAN("Fan"),
    LIGHT("Light"),
}

enum class AmenityType {
    INTERNAL,
    INTERNAL_COUNTABLE,
    SOCIAL,
    SOCIAL_COUNTABLE
}