package com.example.houserentalapp.domain.model.enums

enum class PropertyKind {
    RESIDENTIAL,
    COMMERCIAL
}

enum class PropertyType {
    APARTMENT,
    INDEPENDENT_HOUSE,
    VILLA,
    FARM_HOUSE,
    STUDIO,
    OTHER,
}

enum class FurnishingType {
    UN_FURNISHED,
    SEMI_FURNISHED,
    FULLY_FURNISHED,
}

enum class BHK(val displayName: String) {
    ONE_RK("1RK"),
    ONE_BHK("1BHK"),
    TWO_BHK("2BHK"),
    THREE_BHK("3BHK"),
    FOUR_BHK("4BHK"),
    FIVE_PLUS_BHK("5+BHK");

    override fun toString(): String {
        return displayName
    }

    companion object {
        fun fromDisplayName(name: String): BHK? = entries.find { it.displayName == name }
    }
}

enum class TenantType {
    ALL,
    FAMILY,
    BACHELORS,
}

enum class BachelorType {
    BOTH,
    MEN,
    WOMEN,
}

enum class PropertyTransactionType {
    NEW_BOOKING,
    RESALE,
}

enum class LookingTo {
    RENT,
    BUY,
    LEASE, // Optional
    SELL,
}