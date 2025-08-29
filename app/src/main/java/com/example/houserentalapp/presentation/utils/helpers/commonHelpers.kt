package com.example.houserentalapp.presentation.utils.helpers

import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun setSystemBarBottomPadding(view: View) {
    ViewCompat.setOnApplyWindowInsetsListener(view){ _, insets ->
        val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        Log.d("setSystemBarBottomPadding", "systemBarInsets bottom: ${systemBarInsets.bottom} px")
        view.setPadding(
            view.paddingLeft,
            view.paddingTop,
            view.paddingRight,
            systemBarInsets.bottom
        )

        insets
    }
}

fun getRequiredStyleLabel(label: String): SpannableString {
    val spannable = SpannableString("$label *")
    spannable.setSpan(
        ForegroundColorSpan(Color.RED),
        spannable.length - 1,
        spannable.length,
        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE // Both start and end are exclusive
    )
    return spannable
}