package com.example.houserentalapp.presentation.utils.extensions

import android.util.Log
import androidx.fragment.app.FragmentActivity

fun FragmentActivity.logInfo(message: String, throwable: Throwable? = null) {
    Log.i(this.localClassName, message, throwable)
}