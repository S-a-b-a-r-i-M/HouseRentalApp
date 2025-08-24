package com.example.houserentalapp.presentation.utils

import com.example.houserentalapp.domain.utils.ErrorCode

sealed class ResultUI<out T> {
    data class Success<T>(val data: T, val meta: Map<String, Any>? = null): ResultUI<T>()
    data class Error(
        val message: String,
        val errorCode: ErrorCode? = null,
        val exception: Exception? = null
    ) : ResultUI<Nothing>()
    object Loading : ResultUI<Nothing>()
}