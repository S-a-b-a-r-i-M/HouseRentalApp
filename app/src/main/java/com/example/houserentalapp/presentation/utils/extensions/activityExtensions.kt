package com.example.houserentalapp.presentation.utils.extensions

import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.ShapeAppearanceModel


fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun AppCompatActivity.clearBackStackHistory(tag: String, immediate: Boolean = false) {
    supportFragmentManager.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    if (immediate) // The above popBackStack is async.Hence, want to exec immediately before doing next action
        supportFragmentManager.executePendingTransactions()
}

fun AppCompatActivity.loadFragment(
    fragment: Fragment,
    containerId: Int,
    pushToBackStack: Boolean = false,
    removeHistory: Boolean = false,
) {
    if (removeHistory)
        clearBackStackHistory(fragment.simpleClassName, pushToBackStack)

    supportFragmentManager.beginTransaction().apply {
        replace(containerId, fragment)
        if (pushToBackStack) addToBackStack(fragment.simpleClassName) // adding the current fragment/activity into the backstack
        commit()
    }
}

fun AppCompatActivity.addFragment(
    fragment: Fragment,
    containerId: Int,
    pushToBackStack: Boolean = false,
    removeHistory: Boolean = false
) {
    // EXISTING FRAGMENT
    val existingFragment = supportFragmentManager.findFragmentById(containerId)

    if (removeHistory)
        clearBackStackHistory(fragment.simpleClassName, pushToBackStack)

    supportFragmentManager.beginTransaction().apply {
        add(containerId, fragment)
        if (pushToBackStack) addToBackStack(fragment.simpleClassName) // adding the current fragment/activity into the backstack
        commit()
    }
}

fun Context.getShapableImageView(imageWidth: Int) : ShapeableImageView {
    return ShapeableImageView(this).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        shapeAppearanceModel = ShapeAppearanceModel.builder().apply {
            setAllCornerSizes(24f)
        }.build()

        val marginInPx = 5.dpToPx()
        setLayoutParams(
            LinearLayout.LayoutParams(
                imageWidth,
                LinearLayout.LayoutParams.MATCH_PARENT
            ).apply {
                setMargins(marginInPx, 0, marginInPx, 0)
            }
        )
    }
}