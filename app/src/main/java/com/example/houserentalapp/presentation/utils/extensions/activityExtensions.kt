package com.example.houserentalapp.presentation.utils.extensions

import android.app.Activity
import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.ShapeAppearanceModel


fun Activity.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun AppCompatActivity.loadFragment(
    containerId: Int,
    fragment: Fragment,
    pushToBackStack: Boolean = false
) {
    supportFragmentManager.beginTransaction().apply {
        replace(containerId, fragment)
        if (pushToBackStack) addToBackStack(fragment.simpleClassName) // ADDING THE CURRENT FRAGMENT/ACTIVITY INTO THE BACKSTACK
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

fun Context.getShapableImageView(imageWidth: Int) : ShapeableImageView {
    return ShapeableImageView(this).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        shapeAppearanceModel = ShapeAppearanceModel.builder().apply {
            setAllCornerSizes(24f)
        }.build()

        val marginInPx = 5.dpToPx(context)
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