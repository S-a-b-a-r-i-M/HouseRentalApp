
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // alias(libs.plugins.kotlin.parcelize)
    id("org.jetbrains.kotlin.plugin.parcelize") /*
        Plugin: Compile-time code generation (adds @Parcelize annotation processing)
        Dependency: Runtime libraries
        Parcelize generates Parcelable implementation code during compilation, so it's a build-time tool
    */
    alias(libs.plugins.dagger.hilt)
    kotlin("kapt") /*
        kotlin("kapt") enables Kotlin’s annotation processing tool in your Gradle build,
        letting libraries like Room, Dagger/Hilt, Glide generate code at compile time.
        Without it → your project won’t compile if those libraries need generated classes.
    */
}

android {
    namespace = "com.example.houserentalapp"
    compileSdk = 36  // What you compile against

    defaultConfig {
        applicationId = "com.example.houserentalapp"
        minSdk = 30 // Android 11  // Minimum supported device
        targetSdk = 36 // What you're optimized for
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.legacy.support.v4)
    // For image loading and caching
    implementation("com.github.bumptech.glide:glide:4.15.1")
    kapt("com.github.bumptech.glide:compiler:4.15.1")
    // GSON
    implementation("com.google.code.gson:gson:2.10.1")
    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")
    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Unit Testing
    testImplementation(libs.junit) // testImplementation adds dependencies for local tests(Unit Tests)

    // Instrument Testing
        // Espresso
    androidTestImplementation(libs.androidx.junit) // androidTestImplementation adds dependencies for Instrumented tests.

    androidTestImplementation(libs.androidx.espresso.core) /*
        espresso-core (androidTestImplementation)
        Why: Main Espresso library - basic UI interactions
        When: Always needed for UI tests - provides onView(), click(), typeText(), etc.
    */
    androidTestImplementation(libs.androidx.espresso.contrib) /*
        espresso-contrib (androidTestImplementation)
        Why: Extra UI components support (RecyclerView, DrawerLayout, DatePicker)
        When: Testing lists, navigation drawers, or complex widgets
    */
    androidTestImplementation(libs.androidx.espresso.intents)
        // UiAutomator
    androidTestImplementation(libs.androidx.uiautomator)
}