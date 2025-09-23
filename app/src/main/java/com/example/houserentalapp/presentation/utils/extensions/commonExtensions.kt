package com.example.houserentalapp.presentation.utils.extensions

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import androidx.core.net.toUri
import java.io.File

/*** converts density-independent pixels (dp) to actual pixels (px). ***/
fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun String.deleteFile() {
    try {
        val file = File(this).delete()
        logInfo("File($this) deleted successfully. ")
    } catch (exp: Exception) {
        logWarning("File($this) not deleted")
    }
}

fun Context.openDialer(phone: String) {
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = "tel:$phone".toUri()
    }
    startActivity(intent)
}

fun Context.openMail(email: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:".toUri()  // ensures only email apps respond
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
    }
    startActivity(intent)
}
