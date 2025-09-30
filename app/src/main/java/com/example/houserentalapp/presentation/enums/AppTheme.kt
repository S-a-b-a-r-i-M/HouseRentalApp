package com.example.houserentalapp.presentation.enums

import androidx.annotation.StyleRes
import com.example.houserentalapp.R
import com.example.houserentalapp.domain.model.enums.ReadableEnum

enum class AppTheme(override val readable: String, @StyleRes val theme: Int) : ReadableEnum {
    BLUE("Blue", R.style.AppTheme_Blue),
    VIOLET("Violet", R.style.AppTheme_Violet);

    companion object : ReadableEnum.Companion<AppTheme> {
        override val values: Array<AppTheme> = entries.toTypedArray()
    }
}