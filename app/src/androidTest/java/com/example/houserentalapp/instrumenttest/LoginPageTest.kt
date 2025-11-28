package com.example.houserentalapp.instrumenttest

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.houserentalapp.R
import com.example.houserentalapp.presentation.ui.auth.AuthActivity
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginPageTest {

    private val validUser1 = mapOf(
        "phone" to "9876543210",
        "password" to "Password1234"
    )
    private val validUser2 = mapOf(
        "phone" to "9877089777",
        "password" to "Password1234"
    )

    @get:Rule
    val activityRule = ActivityScenarioRule(AuthActivity::class.java)

    private var decorView: View? = null

    @Before
    fun setDecorView() {
        activityRule.scenario.onActivity(object : ActivityScenario.ActivityAction<AuthActivity> {
            override fun perform(activity: AuthActivity?) {
                decorView = activity?.window?.decorView
            }
        })
    }

    private val etPhoneViewInt get() = onView(withId(R.id.etPhone))
    private val etPasswordViewInt get() = onView(withId(R.id.etPassword))

    @Test
    fun withoutPhoneAndEmail() {
        // Click Sign-in
        clickSignInButton()

        // Should Show 2 required error message
        onView(withId(R.id.tilPhone))
            .check(matches(hasTextInputLayoutErrorText("required")))
        onView(withId(R.id.tilPassword))
            .check(matches(hasTextInputLayoutErrorText("required")))
    }

    @Test
    fun lessThan9DigitPhone() {
        etPhoneViewInt
            .perform(typeText(validUser1.getValue("phone")))

        // Click Sign-in
        clickSignInButton()

        // Assert For Error
        onView(withId(R.id.tilPhone))
            .check(matches(hasTextInputLayoutErrorText("must be at least 9 digits")))
    }

    @Test
    fun validPhoneWithInValidPassword() {
        etPhoneViewInt
            .perform(typeText(validUser1.getValue("phone")))
        etPasswordViewInt
            .perform(typeText("Pass"), closeSoftKeyboard())

        // Click Sign-in
        clickSignInButton()

        // Assert For Error
        onView(withId(R.id.tilPassword))
            .check(matches(hasTextInputLayoutErrorText("must be at least 8 characters")))
    }

    @Test
    fun validUserLogin() {
        etPhoneViewInt
            .perform(typeText(validUser1.getValue("phone")))
        etPasswordViewInt
            .perform(typeText(validUser1.getValue("password")), closeSoftKeyboard())

        // Click Sign-in
        clickSignInButton()

        // Assert User Logged In
        //Thread.sleep(1000)

        // Check Welcome Message Is Displayed
        onView(withContentDescription(R.string.welcome_msg))
            .check(matches(isDisplayed()))

        // Check Bottom Nav Profile Option Visibility
        onView(withId(R.id.bnav_profile))
            .check(matches(isDisplayed()))

        // Navigate To Profile
        onView(withId(R.id.bnav_profile))
            .perform(click())

        // Check The Phone Number Matches
        onView(withId(R.id.tvUserPhone))
            .check(matches( withText(validUser1.getValue("password")) ))


    }

    /*
    @Test
    fun unknownUserPhone() {
        etPhoneViewInt
            .perform(typeText(validUser1.getValue("phone") + "7"))
        etPasswordViewInt
            .perform(typeText(validUser1.getValue("password")))

        // Click Sign-in
        clickSignInButton()

        onView(withText("No User found for given phone"))
            .inRoot(withDecorView(Matchers.not(decorView)))
            .check(matches(isDisplayed()))
    }
     */

    // ************* HELPER METHODS *************
    fun hasTextInputLayoutErrorText(expectedError: String): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Expected error: $expectedError")
            }

            override fun matchesSafely(view: View): Boolean {
                if (view !is TextInputLayout) return false
                return view.error == expectedError
            }
        }
    }

    fun clickSignInButton() {
        onView(withId(R.id.btn_sign_in)).perform(click())
    }
}