package com.example.houserentalapp.domain.utils

sealed class Result<out T> {
    data class Success<T>(val data: T): Result<T>()
    data class Error(
        val message: String,
        val errorCode: ErrorCode? = null,
        val exception: Exception? = null
    ) : Result<Nothing>()
}

enum class ErrorCode {
    RESOURCE_NOT_FOUND,
    VALIDATION,
    DUPLICATION,
}