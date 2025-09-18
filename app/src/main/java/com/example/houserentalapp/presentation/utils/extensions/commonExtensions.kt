package com.example.houserentalapp.presentation.utils.extensions

import android.content.Context

fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

fun Int.spToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

fun Int.pxToSP(context: Context): Float = this / context.resources.displayMetrics.density
