package com.example.houserentalapp.domain.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun String.toEpoch(format: String = "dd/MM/yyyy"): Long {
    val formatter = DateTimeFormatter.ofPattern(format)
    val localDate = LocalDate.parse(this, formatter)
    return localDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
//    return localDate.toEpochSecond(ZoneId.systemDefault())
}

fun formatEpochTime(epochTime: Long) {
    val instant = Instant.ofEpochSecond(epochTime)
    val format1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val format2 = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    println("ZoneId.systemDefault(): ${ZoneId.systemDefault()}")
    val zonedDateTime = instant.atZone(ZoneId.systemDefault())
    println(zonedDateTime.format(format1))
    println(zonedDateTime.format(format2))
}