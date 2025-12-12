package com.example.houserentalapp.instrumenttest

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File


@RunWith(AndroidJUnit4::class)
class LoginPageTest_UiAutomator {

    private lateinit var device: UiDevice // Here We are having the access to whole device
    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext // Your App's Context
    /* targetContext vs context */

    @Before
    fun setup() {
        // Get the UiDevice instance - this represents your Android device
        // InstrumentationRegistry gives us access to the test environment
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Press the home button to start from a known state
        device.pressHome()

        // Wait for the launcher to appear (the home screen)
        // This ensures we're starting from a clean state
        device.wait(
            Until.hasObject(By.pkg(device.launcherPackageName)),
            3000
        )
    }

    private val validUser1 = mapOf(
        "phone" to "9876543210",
        "password" to "Password1234"
    )
    private val validUser2 = mapOf(
        "phone" to "9877089777",
        "password" to "Password1234"
    )
    private val myAppPackage = "com.example.houserentalapp"

    fun openTargetApp(targetPackage: String) {
        // Launch your app by package name
        context.packageManager.getLaunchIntentForPackage(targetPackage)?.let { intent ->
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        }

        // Wait for the app to appear - timeout after 5 seconds (note: UIAutomator doesn't automatically wait)
        device.wait(Until.hasObject(By.pkg(myAppPackage)), 3000)
    }


    @Test
    fun demonstrateFindingElements() {
        context.packageManager.getLaunchIntentForPackage(myAppPackage)?.let { intent ->
            context.startActivity(intent)
        }
        device.wait(Until.hasObject(By.pkg(myAppPackage)), 3000)

        // Find by text - the most straightforward approach
        // Use this when you know the exact text that will appear
        val signinButton = device.findObject(By.text("Sign In"))
        assert(signinButton != null)

        // Find by text with regex - useful for dynamic text
        // For example, if you have "Welcome, John" and the name changes
        val dontHaveAccTxt = device.findObject(By.textStartsWith("Don't have an acc"))
        assert(dontHaveAccTxt != null)

        // Find by resource ID - more stable than text
        // Text might change with translations, but resource IDs stay the same
        // Format: "packagename:id/resource_id"
        val signinBtn = device.findObject(By.res("${myAppPackage}:id/btn_sign_in"))
        assert(signinBtn != null)

        val imaginaryBtn = device.findObject(By.res("${myAppPackage}:id/imagination"))
        assert(imaginaryBtn == null)

        // Find by content description - good for accessibility
        val homeIcon = device.findObject(By.desc("Home"))
        assert(homeIcon != null)

        // Find by class name - useful for generic UI elements
        val anyEditText = device.findObject(By.clazz("android.widget.EditText"))
        assert(anyEditText != null)

        // Combine multiple criteria - this is very powerful
        // Find an EditText with a specific resource ID
        val specificBtn = device.findObject(
            By.clazz("android.widget.TextView")
                .res("com.example.houserentalapp:id/tvSignUp")
        )
        assert(specificBtn != null)

        // Find within a parent - hierarchical search
        // First find a container, then search within it
        val container = device.findObject(By.res("com.example.houserentalapp:id/tilPassword"))
        val showPasswordBtn = container?.findObject(By.res("com.example.houserentalapp:id/text_input_end_icon"))
        assert(showPasswordBtn != null)
    }

    @Test
    fun withoutPhoneAndEmail() {
        openTargetApp(myAppPackage)

        // Click Sign-in Button
        device.findObject(By.res("${myAppPackage}:id/btn_sign_in"))?.let { signInBtn ->
            signInBtn.click()
        }

        // Verify required error appeared
        val error = device.findObject(By.text("required"))
        assert(error != null)
    }

    @Test
    fun validUserLogIn() {
        openTargetApp(myAppPackage)

        // Long click - hold for a specified duration
        val itemToLongPress = device.findObject(By.text("Menu Item"))
        itemToLongPress?.longClick()

        // Text input - type into a field
        val phoneField = device.findObject(By.res("${myAppPackage}:id/etPhone"))
        phoneField?.text = "9090909090"  // Set text directly

        val passwordField = device.findObject(By.res("${myAppPackage}:id/etPassword"))
        passwordField?.text = validUser1.getValue("password")

        // clear Phone and then type
        phoneField?.clear()
        phoneField?.text = validUser1.getValue("phone")

        // Check if element is enabled, clickable, etc.
        val signInBtn = device.findObject(By.text("Sign In"))
        if (signInBtn?.isEnabled == true && signInBtn.isClickable)
            signInBtn.click()
    }

    @Test
    fun demonstrateDeviceActions() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Press hardware buttons
        /*
        device.pressHome()        // Home button
        device.pressBack()        // Back button
        device.pressRecentApps()  // Recent apps button
        device.pressMenu()        // Menu button (on older devices)
         */

        // Open quick settings
        // device.openQuickSettings()

        // Rotate the device
        /*
        device.setOrientationNatural()   // Portrait
        device.setOrientationLeft()      // Landscape left
        device.setOrientationRight()     // Landscape right
        device.freezeRotation()          // Lock rotation
        device.unfreezeRotation()        // Allow rotation
         */

        // Take a screenshot
        val screenshotFile = File("/sdcard/screenshot.png")
        device.takeScreenshot(screenshotFile)

        // Wait for idle - waits until UI is not animating
        device.waitForIdle(3000)  // Timeout in milliseconds
    }
}