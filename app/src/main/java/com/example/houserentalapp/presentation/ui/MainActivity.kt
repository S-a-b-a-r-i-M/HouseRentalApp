package com.example.houserentalapp.presentation.ui

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.ActivityMainBinding
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.presentation.enums.NavigationOptions
import com.example.houserentalapp.presentation.ui.home.HomeFragment
import com.example.houserentalapp.presentation.ui.listings.ListingsFragment
import com.example.houserentalapp.presentation.ui.profile.ProfileFragment
import com.example.houserentalapp.presentation.ui.property.PropertiesListFragment
import com.example.houserentalapp.presentation.ui.property.viewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.showToast
import com.example.houserentalapp.presentation.utils.extensions.simpleClassName

/*  TODO:
        Existing fix:
        2. Have to add batch count in lot of places
        3. Create an base fragment for adding system bars width(if needed)
        4. Handle proper validation in create form and in UI
        5. Create property images edit
        New:
        1. Favourites page -> move and remove properties
        3. My Properties Page -> edit details.
 */

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val sharedDataViewModel: SharedDataViewModel by viewModels()

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
        super.onCreate(savedInstanceState)
        logInfo("<-------- MainActivity onCreate ---------->")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        setWindowInsets()

        // Set Current User Into Shared ViewModel
        val currentUser = intent.getParcelableExtra<User>(CURRENT_USER_KEY) ?: run {
            logError("Current User is not found in intent")
            OnBackPressedDispatcher().onBackPressed()
            return
        }
        sharedDataViewModel.setCurrentUser(currentUser)

//        onBackPressedDispatcher.addCallback(backPressedCallback)

//        runBlocking {
//            UserRepoImpl(this@MainActivity).createUser(
//                "Sabari", "", "", ""
//            )
//        }

        setBottomNavigation()
        if (savedInstanceState == null)
            loadFragment(HomeFragment())
    }

    private fun setWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, imeInsets.bottom)
            insets
        }
    }

    private fun setBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.bnav_home -> {
                    loadFragment(HomeFragment())
                }
                R.id.bnav_shortlists -> {
                    with(sharedDataViewModel) {
                        resetPropertiesListStore()
                        addToPropertiesListStore(PropertiesListFragment.ONLY_SHORTLISTED_KEY, true)
                        addToPropertiesListStore(PropertiesListFragment.HIDE_TOOLBAR_KEY, true)
                        addToPropertiesListStore(PropertiesListFragment.HIDE_FAB_BUTTON_KEY, true)
                    }
                    loadFragment(PropertiesListFragment())
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

    fun selectBottomNavOption(option: NavigationOptions) {
        binding.bottomNavigation.selectedItemId = option.id
    }

    fun showBottomNav() {
        binding.bottomNavigationContainer.visibility = View.VISIBLE
    }

    fun hideBottomNav() {
        binding.bottomNavigationContainer.visibility = View.GONE
    }

    fun loadFragment(
        fragment: Fragment,
        pushToBackStack: Boolean = false,
        containerId: Int = binding.pageFragmentContainer.id
    ) {
        supportFragmentManager.beginTransaction().apply {
            replace(containerId, fragment)
            if (pushToBackStack) addToBackStack(fragment.simpleClassName) // ADDING THE CURRENT FRAGMENT/ACTIVITY INTO THE BACKSTACK
            commit()
        }
    }

    fun addFragment(
        fragment: Fragment,
        pushToBackStack: Boolean = false,
        removeHistory: Boolean = false,
        containerId: Int = binding.pageFragmentContainer.id
    ) {
        // EXISTING FRAGMENT
        val existingFragment = supportFragmentManager.findFragmentById(containerId)

        if (removeHistory)
            supportFragmentManager.popBackStack(fragment.simpleClassName, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        supportFragmentManager.beginTransaction().apply {
            add(containerId, fragment)
            if (pushToBackStack) addToBackStack(fragment.simpleClassName) // ADDING THE CURRENT FRAGMENT/ACTIVITY INTO THE BACKSTACK
            commit()
        }
    }

    companion object {
        const val CURRENT_USER_KEY = "currentUser"
    }
}