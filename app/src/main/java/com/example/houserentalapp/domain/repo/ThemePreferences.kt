package com.example.houserentalapp.domain.repo
import com.example.houserentalapp.domain.utils.Result

interface ThemePreferences {
    fun saveTheme(theme: String) : Result<Unit>

    fun getTheme() : Result<String?>
}