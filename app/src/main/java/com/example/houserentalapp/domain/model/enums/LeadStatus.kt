package com.example.houserentalapp.domain.model.enums

enum class LeadStatus {
    NEW, // Initial
    FOLLOWUP,
    SITE_VISIT,
    DEAL_SUCCESS,
    REJECTED,
}

enum class UserActionEnum(override val readable: String): ReadableEnum {
    VIEW("view"),
    SHORTLISTED("shortlisted"),
    INTERESTED("interested");

    companion object : ReadableEnum.Companion<UserActionEnum> {
        override val values: Array<UserActionEnum> = entries.toTypedArray()
    }
}