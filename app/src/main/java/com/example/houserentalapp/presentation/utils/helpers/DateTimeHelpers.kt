package com.example.houserentalapp.presentation.utils.helpers

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun String.toEpochSeconds(format: String = "dd/MM/yyyy"): Long {
    val formatter = DateTimeFormatter.ofPattern(format)
    // Parse into LocalDate
    val localDate = LocalDate.parse(this, formatter)
    // Convert to UTC epoch seconds by giving our zone info
    return localDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
}

fun isMilliSeconds(epoch: Long) = epoch.toString().length > 10

fun Long.fromEpoch(format: String = "dd/MM/yyyy"): String {
    val instant = if (isMilliSeconds(this))
        Instant.ofEpochMilli(this)
    else
        Instant.ofEpochSecond(this)

    val zonedDateTime = instant.atZone(ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern(format)
    return formatter.format(zonedDateTime)
}

fun Long.getTimePeriod(): String {
    val currentHour = this.fromEpoch("HH").toInt()
    return when {
        currentHour < 12 -> "\uD83C\uDF24\uFE0F Good Morning"
        currentHour < 18 -> "☀️ Good Afternoon"
        else -> "Hope you had a great day"
    }
}