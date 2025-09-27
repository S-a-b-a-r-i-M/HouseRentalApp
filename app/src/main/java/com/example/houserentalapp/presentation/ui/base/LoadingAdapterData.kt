package com.example.houserentalapp.presentation.ui.base

sealed class LoadingAdapterData<out T> {
    data class Data<T>(val data: T) : LoadingAdapterData<T>()
    object Loader : LoadingAdapterData<Nothing>()
}