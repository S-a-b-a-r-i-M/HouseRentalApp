package com.example.houserentalapp.presentation.utils.extensions

import android.content.Context
import android.net.Uri
import java.io.File

fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

fun Int.spToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

fun Int.pxToSP(context: Context): Float = this / context.resources.displayMetrics.density

fun String.deleteFile() {
    try {
        val file = File(this).delete()
        logInfo("File($this) deleted successfully. ")
    } catch (exp: Exception) {
        logWarning("File($this) not deleted")
    }
}
