package com.example.houserentalapp.presentation.utils.extensions

import android.util.Log
import androidx.fragment.app.FragmentActivity

fun FragmentActivity.logInfo(message: String, throwable: Throwable? = null) {
    Log.i(this.localClassName, message, throwable)
}

fun FragmentActivity.logDebug(message: String, throwable: Throwable? = null) {
    Log.d(this.localClassName, message, throwable)
}

fun FragmentActivity.logWarning(message: String, throwable: Throwable? = null) {
    Log.w(this.localClassName, message, throwable)
}

fun FragmentActivity.logError(message: String, throwable: Throwable? = null) {
    Log.e(this.localClassName, message, throwable)
}