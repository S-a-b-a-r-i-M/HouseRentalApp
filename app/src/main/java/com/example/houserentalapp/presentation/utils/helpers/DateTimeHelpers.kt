package com.example.houserentalapp.presentation.utils.helpers

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateFormats {
    const val MONTH_WITH_LEADING_0 = "MM"    // 01–12
    const val MONTH_WITHOUT_LEADING_0 = "M"   // 1–12
    const val MONTH_ABBREVIATED = "MMM"  // Jan, Feb
    const val MONTH_FULL = "MMMM"        // January, February

    const val DAY_WITHOUT_LEADING_0 = "d"     // 1–31
    const val DAY_WITH_LEADING_0 = "dd"      // 01–31

    const val YEAR_FULL = "yyyy"         // 2025
    const val YEAR_TWO_DIGIT = "yy"      // 25

    const val HOURS_24 = "HH"           // 00 - 23
    const val HOURS_12 = "hh"           // 01 - 12
    const val MINUTES = "mm"            // 00 - 59

    const val DATE_DDMMYYYY = "$DAY_WITH_LEADING_0/$MONTH_WITH_LEADING_0/$YEAR_FULL"
}

fun String.toEpochSeconds(format: String = DateFormats.DATE_DDMMYYYY): Long {
    val formatter = DateTimeFormatter.ofPattern(format)
    // Parse into LocalDate
    val localDate = LocalDate.parse(this, formatter)
    // Convert to UTC epoch seconds by giving our zone info
    return localDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
}

fun isMilliSeconds(epoch: Long) = epoch.toString().length > 10

fun Long.fromEpoch(format: String = DateFormats.DATE_DDMMYYYY): String {
    val instant = if (isMilliSeconds(this))
        Instant.ofEpochMilli(this)
    else
        Instant.ofEpochSecond(this)

    val zonedDateTime = instant.atZone(ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern(format)
    return formatter.format(zonedDateTime)
}

fun Long.getTimePeriod(): String {
    val currentHour = this.fromEpoch(DateFormats.HOURS_24).toInt()
    return when {
        currentHour < 12 -> "\uD83C\uDF24\uFE0F Good Morning"
        currentHour < 18 -> "☀️ Good Afternoon"
        else -> "Hope you had a great day"
    }
}