package com.example.houserentalapp.presentation.utils.extensions

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

val Any.simpleClassName: String
    get() = this::class.java.simpleName

fun getOnlyClassName(classWithPackage: String) = classWithPackage.split(".").last()

fun Activity.logInfo(message: String, throwable: Throwable? = null) {
    Log.i(simpleClassName, message, throwable)
}

fun Activity.logDebug(message: String, throwable: Throwable? = null) {
    Log.d(simpleClassName, message, throwable)
}

fun Activity.logWarning(message: String, throwable: Throwable? = null) {
    Log.w(simpleClassName, message, throwable)
}

fun Activity.logError(message: String, throwable: Throwable? = null) {
    Log.e(simpleClassName, message, throwable)
}

fun Activity.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Fragment.logInfo(message: String, throwable: Throwable? = null) {
    Log.i(simpleClassName, message, throwable)
}

fun Fragment.logDebug(message: String, throwable: Throwable? = null) {
    Log.d(simpleClassName, message, throwable)
}

fun Fragment.logWarning(message: String, throwable: Throwable? = null) {
    Log.w(simpleClassName, message, throwable)
}

fun Fragment.logError(message: String, throwable: Throwable? = null) {
    Log.e(simpleClassName, message, throwable)
}


fun AppCompatActivity.loadFragment(
    containerId: Int,
    fragment: Fragment,
    pushToBackStack: Boolean = false
) {
    // EXISTING FRAGMENT
    val existingFragment = supportFragmentManager.findFragmentById(containerId)

    supportFragmentManager.beginTransaction().apply {
        replace(containerId, fragment)
        if (pushToBackStack) addToBackStack(null) // ADDING THE CURRENT FRAGMENT/ACTIVITY INTO THE BACKSTACK
        commit()
    }


}