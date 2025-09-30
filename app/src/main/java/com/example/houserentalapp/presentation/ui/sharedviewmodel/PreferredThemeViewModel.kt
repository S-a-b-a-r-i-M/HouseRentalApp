package com.example.houserentalapp.presentation.ui.sharedviewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.houserentalapp.data.repo.ThemePreferencesImpl
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.enums.AppTheme

class PreferredThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = ThemePreferencesImpl(application)
    private var currentTheme: AppTheme? = null
    private var isThemeAlreadyFetched = false
    private val _isThemeUpdated = MutableLiveData(false)
    val isThemeUpdated: LiveData<Boolean> = _isThemeUpdated

    fun getTheme(): AppTheme? {
        if (isThemeAlreadyFetched)
            return currentTheme

        return when(val res = repo.getTheme()){
            is Result.Success<String?> -> {
                currentTheme = if (res.data != null)
                        AppTheme.fromString(res.data)
                    else
                        null
                isThemeAlreadyFetched = true
                currentTheme
            }
            is Result.Error -> {
                null
            }
        }
    }

    fun saveTheme(theme: AppTheme) {
        when(repo.saveTheme(theme.readable)) {
            is Result.Success<*> -> {
                currentTheme = theme // Updating Selected Theme
                _isThemeUpdated.value = true // Trigger To Apply New Theme
            }
            is Result.Error -> {}
        }
    }

    fun clearThemUpdate() {
        _isThemeUpdated.value = false
    }
}