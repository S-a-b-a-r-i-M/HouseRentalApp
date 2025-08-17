package com.example.houserentalapp.presentation.utils.helpers

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