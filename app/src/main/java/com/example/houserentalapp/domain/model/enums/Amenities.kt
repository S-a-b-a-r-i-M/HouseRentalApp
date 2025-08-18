package com.example.houserentalapp.domain.model.enums

enum class SocialAmenitiesEnum {
    POWER_BACKUP,
    SWIMMING_POOL,
    GYM,
    LIFT,
    GARDEN,
    PLAY_AREA,
    CCTV,
    GATED_COMMUNITY,
    COMMUNITY_HALL,
    REGULAR_WATER_SUPPLY,
}

enum class InternalAmenitiesEnum {
    SOFA,
    STOVE,
    FRIDGE,
    CHIMNEY,
    MICROWAVE,
    DINING_TABLE,
    WASHING_MACHINE,
    WATER_PURIFIER,
    WATER_HEATER,
    WIFI
}

enum class CountableInternalAmenitiesEnum {
    AC,
    TV,
    BED,
    FAN,
    LIGHT,
}

enum class AmenityType {
    INTERNAL,
    INTERNAL_COUNTABLE,
    SOCIAL,
    SOCIAL_COUNTABLE
}