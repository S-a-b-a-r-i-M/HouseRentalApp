package com.example.houserentalapp.presentation.ui

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.houserentalapp.R
import com.example.houserentalapp.data.local.db.DatabaseHelper
import com.example.houserentalapp.data.repo.UserRepoImpl
import com.example.houserentalapp.databinding.ActivityStarterBinding
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.model.UserPreferences
import com.example.houserentalapp.domain.model.enums.LookingTo
import com.example.houserentalapp.domain.repo.UserRepo
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.ui.home.HomeFragment
import com.example.houserentalapp.presentation.ui.listings.ListingsFragment
import com.example.houserentalapp.presentation.ui.profile.ProfileFragment
import com.example.houserentalapp.presentation.ui.shortlisted.ShortlistsFragment
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.showToast
import com.example.houserentalapp.presentation.utils.helpers.PasswordHasher
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking

class StarterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStarterBinding

    // TASK: IF USER CLICKS BACK BUTTON ON OTHER FRAGMENTS EXCEPT HOME WE HAVE TO NAVIGATE THEM TO HOME
    private val backPressedCallback = object : OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
            println("handleOnBackPressed ----> ")
            remove() // Remove on first click
            loadFragment(HomeFragment()) // Move to Home
            showToast("Press again to exit...")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        logInfo("<-------- onCreate ---------->")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityStarterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setWindowInsets()

//        onBackPressedDispatcher.addCallback(backPressedCallback)

        setBottomNavigation()
        if (savedInstanceState == null)
            loadFragment(HomeFragment())

        val userRepo: UserRepo = UserRepoImpl(this)
        /*
        runBlocking {
            val phone = "9942976912"
            val createUserResult =  userRepo.createUser(
               "sabari",
               "9942976912",
               "sabari@gmail.com",
               PasswordHasher.getHashPassword("fsdfsafgs4324v")
            )
            when(createUserResult) {
                is Result.Success<Long> -> {
                    val userId: Long = createUserResult.data
                    logInfo("User($userId) Created")

                    // get user
                    when (val result = userRepo.getUserById(userId)) {
                        is Result.Success<User?> -> {
                            if (result.data == null)
                                throw IllegalStateException("No user found for userid: $userId")

                            logInfo("User retrieved (${result.data}) Created")

                            // create UserPreferences
                            val userPreferencesResult = userRepo.createUserPreferences(
                                userId, UserPreferences(null, null, null)
                            )
                            when (userPreferencesResult) {
                                is Result.Success<Long> -> {
                                    logInfo("User Preferences (${userPreferencesResult.data}) Created")
                                }
                                is Result.Error -> {
                                    logError(userPreferencesResult.message)
                                }
                            }

                            // get UserPreferences
                            when (val result2 = userRepo.getUserPreferences(userId)) {
                                is Result.Success<UserPreferences?> -> {
                                    if (result2.data == null)
                                        throw IllegalStateException("No user found for userid: $userId")

                                    logInfo("User Preferences (${result2.data}) retrieved")
                                }
                                is Result.Error -> {
                                    logError(result2.message)
                                }
                            }

                            // update user prefs
                            when (val result3 = userRepo.updateUserPreferences(
                                userId,
                                UserPreferences(null, null, LookingTo.RENT)
                            )) {
                                is Result.Success<Boolean> -> {
                                    if (!result3.data)
                                        throw IllegalStateException("prefrence update failed")

                                    logInfo("User Preferences updated")
                                }
                                is Result.Error -> {
                                    logError(result3.message)
                                }
                            }
                        }
                        is Result.Error -> logError(result.message)
                    }

                    // get user by phone
                    when (val result = userRepo.getUserByPhone("9942976912")) {
                        is Result.Success<User?> -> {
                            if (result.data == null)
                                throw IllegalStateException("No user found for phone")

                            logInfo("User retrieved (${result.data})")
                        }
                        is Result.Error -> logError(result.message)
                    }


                }
                is Result.Error -> logError(createUserResult.message)
            }

            when(val result = userRepo.isPhoneNumberExists(phone)) {
                is Result.Success<Boolean> -> {
                    println("isPhoneNumberExists Success")
                }
                is Result.Error -> {
                    println("isPhoneNumberExists Error")
                }
            }


       }
         */

    }

    private fun setWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.starter)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, imeInsets.bottom)
            insets
        }
    }

    private fun scrollToFocusedView() {
        val currentFocus = currentFocus
//        if (currentFocus is EditText) {
//            val scrollView: NestedScrollView = binding.scrollView
//            scrollView?.post {
//                val rect = Rect()
//                currentFocus.getGlobalVisibleRect(rect)
//                scrollView.requestChildRectangleOnScreen(currentFocus, rect, false)
//            }
//        }
    }

    private fun setBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.bnav_home -> {
                    loadFragment(HomeFragment())
                }
                R.id.bnav_shortlists -> {
                    loadFragment(ShortlistsFragment())
                }
                R.id.bnav_listings -> {
                    loadFragment(ListingsFragment())
                }
                R.id.bnav_profile -> {
                    loadFragment(ProfileFragment())
                }
            }

            return@setOnItemSelectedListener true
        }

        binding.bottomNavigation.setOnItemReselectedListener { item ->
            logInfo("setOnItemReselectedListener")
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(binding.pageFragmentContainer.id, fragment)
            .commit()
    }
}