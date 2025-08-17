package com.example.houserentalapp.presentation.utils.extensions

import android.util.Log

val Any.simpleClassName: String
    get() = this::class.java.simpleName

fun getOnlyClassName(classWithPackage: String) = classWithPackage.split(".").last()

fun Any.logInfo(message: String, throwable: Throwable? = null) {
    Log.i(simpleClassName, message, throwable)
}

fun Any.logDebug(message: String, throwable: Throwable? = null) {
    Log.d(simpleClassName, message, throwable)
}

fun Any.logWarning(message: String, throwable: Throwable? = null) {
    Log.w(simpleClassName, message, throwable)
}

fun Any.logError(message: String, throwable: Throwable? = null) {
    Log.e(simpleClassName, message, throwable)
}