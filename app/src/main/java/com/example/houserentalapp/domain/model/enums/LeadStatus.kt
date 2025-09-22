package com.example.houserentalapp.domain.model.enums

enum class LeadStatus(override val readable: String): ReadableEnum {
    NEW("New"),
    FOLLOWUP("Follow Up"),
    SITE_VISIT("Site Visit"),
    DEAL_SUCCESS("Deal Success"),
    REJECTED("Rejected");

    companion object : ReadableEnum.Companion<LeadStatus> {
        override val values: Array<LeadStatus> = entries.toTypedArray()
    }
}

enum class UserActionEnum(override val readable: String): ReadableEnum {
    VIEW("view"),
    SHORTLISTED("shortlisted"),
    INTERESTED("interested");

    companion object : ReadableEnum.Companion<UserActionEnum> {
        override val values: Array<UserActionEnum> = entries.toTypedArray()
    }
}