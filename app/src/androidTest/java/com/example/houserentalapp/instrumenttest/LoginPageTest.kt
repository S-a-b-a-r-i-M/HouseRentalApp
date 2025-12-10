package com.example.houserentalapp.instrumenttest

import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
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
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.matchesPattern
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
            .check(matches( withText(validUser1.getValue("phone")) ))

        // Click Log Out -- Opens Dialog
        onView(allOf(
            withContentDescription("Log Out"),
            isDescendantOfA(withId(R.id.toolbar))
        )).perform(click())
            .inRoot(isDialog())

        // Click Log out Option -- Redirects to Login Page
        onView(allOf(withText("Logout"), withId(android.R.id.button1)))
            .perform(click())
        onView(withText(matchesPattern("(?i)sign.*in")))  // case-insensitive regex
            .check(matches(isDisplayed()))
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

    // TODO: Try with SignUp text
    fun withTextColor(@ColorInt expectedColor: Int): Matcher<View> {
        return object : BoundedMatcher<View, TextView>(TextView::class.java) {
            // This is where the actual matching logic happens
            override fun matchesSafely(item: TextView?): Boolean {
                return item?.currentTextColor == expectedColor
            }

            // This describes what the matcher is looking for (used in error messages)
            override fun describeTo(description: Description?) {
                description?.appendText("textview with expected color: $expectedColor")
            }
        }
    }

    fun isVisibleAndInteractable(minAlpha: Float = 1.0f): Matcher<View> {
        return object : BoundedMatcher<View, View>(View::class.java) {
            override fun matchesSafely(item: View?): Boolean {
                return if (item != null) {
                    val hasEnoughAlpha = item.alpha >= minAlpha
                    return item.isVisible && hasEnoughAlpha && item.isEnabled
                }
                else
                    false
            }

            override fun describeTo(description: Description?) {
                description?.appendText("is visible, enabled, minAlpha >= $minAlpha")
            }

            /*The describeMismatch method is optional but incredibly valuable.
              When your test fails, it tells you exactly why the matcher didn't match, making debugging much faster.*/
            override fun describeMismatch(item: Any?, description: Description?) {
                if (item is View && description != null) {
                    description.appendText("was ")
                    when {
                        !item.isVisible -> description.appendText("not visible")
                        !item.isEnabled -> description.appendText("not enabled")
                        item.alpha < minAlpha -> description.appendText("not having min alpha")
                    }
                }
            }
        }
    }

    fun clickSignInButton() {
        onView(withId(R.id.btn_sign_in)).perform(click())
    }
}