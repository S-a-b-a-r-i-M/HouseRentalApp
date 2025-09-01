package com.example.houserentalapp.domain.model.enums

interface AmenityBaseEnum : ReadableEnum

enum class SocialAmenity(override val readable: String) : AmenityBaseEnum {
    POWER_BACKUP("Power Backup"),
    SWIMMING_POOL("Swimming Pool"),
    GYM("Gym"),
    LIFT("Lift"),
    PLAY_AREA("Play Area"),
    GATED_COMMUNITY("Gated Community"),
    COMMUNITY_HALL("Community Hall"),
    REGULAR_WATER_SUPPLY("Regular Water Supply");

    companion object : ReadableEnum.Companion<SocialAmenity> {
        override val values: Array<SocialAmenity> = entries.toTypedArray()
    }
}

enum class InternalAmenity(override val readable: String) : AmenityBaseEnum {
    WIFI("WiFi"),
    SOFA("Sofa"),
    FRIDGE("Fridge"),
    CHIMNEY("Chimney"),
    MICROWAVE("Microwave"),
    WASHING_MACHINE("Washing Machine"),
    WATER_PURIFIER("Water Purifier"),
    WATER_HEATER("Water Heater");

    companion object : ReadableEnum.Companion<InternalAmenity> {
        override val values: Array<InternalAmenity> = entries.toTypedArray()
    }
}

enum class CountableInternalAmenity(override val readable: String) : AmenityBaseEnum {
    AC("AC"),
    TV("TV"),
    BED("Bed"),
    FAN("Fan"),
    LIGHT("Light");

    companion object : ReadableEnum.Companion<CountableInternalAmenity> {
        override val values: Array<CountableInternalAmenity> = entries.toTypedArray()
    }
}

enum class AmenityType {
    INTERNAL,
    INTERNAL_COUNTABLE,
    SOCIAL
}