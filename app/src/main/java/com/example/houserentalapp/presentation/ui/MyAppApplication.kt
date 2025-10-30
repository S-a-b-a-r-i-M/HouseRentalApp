package com.example.houserentalapp.presentation.ui

import android.app.Application
import com.example.houserentalapp.presentation.ui.auth.AuthDependencyStore
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyAppApplication : Application() {
    // Manual Dependency Handling
    val authDependencyStore by lazy { AuthDependencyStore(this) }

    override fun onCreate() {
        super.onCreate()
        logDebug("------------ MyApplication's onCreate() invoked -------------")
    }
}