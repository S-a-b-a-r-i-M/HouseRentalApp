package com.example.houserentalapp.presentation.utils.helpers

import android.util.Patterns

fun validateUserName(name: String): String? {
    return when {
        name.isBlank() -> "required"
        name.length < 3 -> "must contains at least 3 characters"
        name.length > 25 -> "should not more than 25 characters"
        else -> null
    }
}

fun validateEmailFormat(email: String): String? {
    return when {
        email.isBlank() -> "required"
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
        else -> null
    }
}

fun validatePhoneFormat(phone: String): String? {
    return when {
        phone.isBlank() -> "required"
        phone.length < 10 -> "must be at least 10 digits"
        !phone.all { it.isDigit() } -> "must contain only numbers"
        else -> null
    }
}

fun validatePasswordStrength(password: String): String? {
    return when {
        password.isBlank() -> "required"
        password.length < 8 -> "must be at least 8 characters"
        !password.any { it.isDigit() } -> "must contain at least one number"
        !password.any { it.isUpperCase() } -> "must contain at least one uppercase letter"
        !password.any { it.isLowerCase() } -> "must contain at least one lowercase letter"
        else -> null
    }
}