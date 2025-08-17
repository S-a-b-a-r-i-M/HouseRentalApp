package com.example.houserentalapp.presentation.utils.extensions

import android.app.Activity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment


fun Activity.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
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

fun AppCompatActivity.addFragment(
    containerId: Int,
    fragment: Fragment,
    pushToBackStack: Boolean = false
) {
    // EXISTING FRAGMENT
    val existingFragment = supportFragmentManager.findFragmentById(containerId)

    supportFragmentManager.beginTransaction().apply {
        add(containerId, fragment)
        if (pushToBackStack) addToBackStack(null) // ADDING THE CURRENT FRAGMENT/ACTIVITY INTO THE BACKSTACK
        commit()
    }
}