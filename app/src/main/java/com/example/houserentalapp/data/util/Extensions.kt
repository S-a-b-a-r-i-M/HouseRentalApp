package com.example.houserentalapp.data.util

import com.example.houserentalapp.presentation.utils.extensions.logError
import kotlin.enums.EnumEntries

fun String.capitalize(): String = this[0].uppercase() + this.substring(1).lowercase()

// Extension function on EnumEntries
fun <T: Enum<T>> EnumEntries<T>.fromString(inputValue: String): T? {
    val trimmedValue = inputValue.trim().uppercase()
    return find { it.name == trimmedValue } ?: run {
        logError("given value($trimmedValue) is not valid enum value.")
        null
    }
}