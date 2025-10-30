// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Purpose: Configures settings for the entire project (all modules)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.dagger.hilt) apply false
}

/*
Contains:
    Plugin versions (for all modules)
    Common repositories
    Project-wide dependencies
    Build script dependencies
*/