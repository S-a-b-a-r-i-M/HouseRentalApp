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
        2. Make Use Cases Single Responsibility (optional)
        3. Get Current User Details from db(MainActivity) and place it in shared view model
        4. Have to add batch count in lot of places
        5. Create an base fragment for adding system bars width(if needed)
        6. Check filters
        New:
        1. Favourites page -> move and remove properties
        2. Filters
        3. My Properties Page -> List of uploaded properties, edit status, edit details.
 */

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var currentUser: User
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

        // Get Current User ID
        val currentUserId = intent.getIntExtra(CURRENT_USER_ID_KEY, -1)
        if (currentUserId == -1) {
            logError("Current User id is not found in intent")
            OnBackPressedDispatcher().onBackPressed()
            return
        }
        // Fetch Current User From ViewModel
        currentUser = User(
            id = 1,
            name = "Owner",
            phone = "994979988",
            email = "owner@gmail.com",
            createdAt = 123465689L
        )
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

    fun getCurrentUser() = currentUser

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
        const val CURRENT_USER_ID_KEY = "currentUserId"
    }
}