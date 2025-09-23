package com.example.houserentalapp.presentation.utils.extensions

import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

enum class DrawablePosition {
    TOP, BOTTOM, LEFT, RIGHT
}

fun TextView.setDrawable(
    @DrawableRes drawableRes: Int,
    widthInDp: Int,
    heightInDp: Int,
    position: DrawablePosition = DrawablePosition.TOP
) {
    val drawable = ContextCompat.getDrawable(context, drawableRes) ?: run {
        logWarning("Drawable is not found.")
        return
    }
    val widthInPx = widthInDp.dpToPx()
    val heightInPx = heightInDp.dpToPx()

    drawable.setBounds(0, 0, widthInPx, heightInPx)
    when(position) {
        DrawablePosition.TOP ->
            setCompoundDrawables(null, drawable, null, null)
        DrawablePosition.BOTTOM ->
            setCompoundDrawables(null, null, null, drawable)
        DrawablePosition.LEFT ->
            setCompoundDrawables(drawable, null, null, null)
        DrawablePosition.RIGHT ->
            setCompoundDrawables(null, null, drawable, null)
    }
}