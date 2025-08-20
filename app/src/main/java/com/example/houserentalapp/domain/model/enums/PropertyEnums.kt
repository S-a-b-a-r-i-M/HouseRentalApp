package com.example.houserentalapp.domain.model.enums

import androidx.annotation.StringRes
import com.example.houserentalapp.R

// TODO: Create base enum

interface ReadableEnum {
    val readable: String
}

enum class PropertyKind(override val readable: String) : ReadableEnum {
    RESIDENTIAL("Residential"),
    COMMERCIAL("Commercial")
}

enum class PropertyType(override val readable: String) : ReadableEnum {
    APARTMENT("Apartment"),
    INDEPENDENT_HOUSE("Independent House"),
    VILLA("Villa"),
    FARM_HOUSE("Farm House"),
    STUDIO("Studio"),
    OTHER("Other");

    companion object {
        fun fromString(inputStr: String): PropertyType? = entries.find {
            it.readable.contentEquals(inputStr, true)
        }

        fun isValid(inputStr: String): Boolean = entries.any {
            it.readable.contentEquals(inputStr, true)
        }
    }
}

enum class FurnishingType(override val readable: String) : ReadableEnum {
    UN_FURNISHED("Unfurnished"),
    SEMI_FURNISHED("Semi Furnished"),
    FULLY_FURNISHED("Furnished");

    companion object {
        fun fromString(inputStr: String): FurnishingType? = entries.find {
            it.readable.contentEquals(inputStr, true)
        }

        fun isValid(inputStr: String): Boolean = entries.any {
            it.readable.contentEquals(inputStr, true)
        }
    }
}

enum class BHK(override val readable: String) : ReadableEnum {
    ONE_RK("1 RK"),
    ONE_BHK("1 BHK"),
    TWO_BHK("2 BHK"),
    THREE_BHK("3 BHK"),
    FOUR_BHK("4 BHK"),
    FIVE_PLUS_BHK("5+ BHK");

    override fun toString(): String {
        return readable
    }

    companion object {
        fun fromString(inputStr: String): BHK? = entries.find {
            it.readable.contentEquals(inputStr, true)
        }

        fun isValid(inputStr: String): Boolean = entries.any {
            it.readable.contentEquals(inputStr, true)
        }
    }
}

enum class TenantType(override val readable: String) : ReadableEnum {
    FAMILY("Family"),
    BACHELORS("Bachelors");

    companion object {
        fun fromString(inputStr: String): TenantType? {
            val trimmedStr = inputStr.trim()
            return entries.find {
                it.readable.contentEquals(trimmedStr, true)
            }
        }

        fun isValid(inputStr: String): Boolean {
            val trimmedStr = inputStr.trim()
            return entries.any {
                it.readable.contentEquals(trimmedStr, true)
            }
        }
    }
}

enum class BachelorType(override val readable: String) : ReadableEnum {
    BOTH("Open for Both"),
    MEN("Men Only"),
    WOMEN("Women Only");

    companion object {
        fun fromString(inputStr: String): BachelorType? = entries.find {
            it.readable.contentEquals(inputStr, true)
        }

        fun isValid(inputStr: String): Boolean = entries.any {
            it.readable.contentEquals(inputStr, true)
        }
    }
}

enum class PropertyTransactionType(override val readable: String) : ReadableEnum {
    NEW_BOOKING("New Booking"),
    RESALE("Resale"),
}

enum class LookingTo(@StringRes val stringResId: Int, val readable: String) {
    RENT(R.string.rent, "Rent"),
    BUY(R.string.buy, "Buy"),
    LEASE(R.string.lease, "Lease"), // Optional
    SELL(R.string.sell, "Sell");

    companion object {
        fun fromString(inputStr: String): LookingTo? = entries.find {
            it.name.contentEquals(inputStr, true)
        }

        fun isValid(inputStr: String): Boolean = entries.any {
            it.readable.contentEquals(inputStr, true)
        }
    }
}