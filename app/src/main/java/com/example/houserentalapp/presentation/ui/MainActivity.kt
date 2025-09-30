package com.example.houserentalapp.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.ActivityMainBinding
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.presentation.ui.auth.AuthActivity
import com.example.houserentalapp.presentation.ui.home.HomeFragment
import com.example.houserentalapp.presentation.ui.interfaces.BottomNavController
import com.example.houserentalapp.presentation.ui.interfaces.FragmentNavigationHandler
import com.example.houserentalapp.presentation.ui.listings.ListingsFragment
import com.example.houserentalapp.presentation.ui.profile.ProfileFragment
import com.example.houserentalapp.presentation.ui.property.PropertiesListFragment
import com.example.houserentalapp.presentation.ui.sharedviewmodel.PreferredThemeViewModel
import com.example.houserentalapp.presentation.ui.sharedviewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.utils.extensions.addFragment
import com.example.houserentalapp.presentation.utils.extensions.loadFragment
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.showToast

/* TODO:
        Existing fix:
         7. Fix Fonts in every place
         8. Improve alert dialog design
        New:
         1. Favourites page -> move and remove properties
*/

class MainActivity : AppCompatActivity(), BottomNavController, FragmentNavigationHandler {
    private lateinit var binding: ActivityMainBinding
    val sharedDataViewModel: SharedDataViewModel by viewModels()
    val preferredThemeViewModel: PreferredThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        preferredThemeViewModel.getTheme()?.let { setTheme(it.theme) } // Set Theme
        super.onCreate(savedInstanceState)
        logInfo("<-------- MainActivity onCreate ---------->")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        setWindowInsets()
        observeViewModel()

        // Set Current User Into Shared ViewModel
        val currentUser = intent.getParcelableExtra<User>(CURRENT_USER_KEY) ?: run {
            logError("Current User is not found in intent")
            OnBackPressedDispatcher().onBackPressed()
            return
        }
        sharedDataViewModel.setCurrentUser(currentUser)

        setBottomNavigation()

        if (savedInstanceState == null)
            loadFragmentInternal(HomeFragment())
    }

    private fun setWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            binding.statusBarOverlayView.layoutParams.height = systemBars.top // ADD STATUS BAR PADDING AS A HEIGHT
            v.setPadding(systemBars.left, 0, systemBars.right, imeInsets.bottom)
            insets
        }
    }

    data class ProgrammaticNavSelection(val extraArgs: Bundle? = null, val pushToBackStack: Boolean = false)
    private var programmaticNavSelection: ProgrammaticNavSelection? = null

    private fun setBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.bnav_home -> {
                    val destination = HomeFragment()
                    programmaticNavSelection?.let {
                        destination.arguments = it.extraArgs
                        loadFragmentInternal(destination, it.pushToBackStack)
                    } ?: loadFragmentInternal(destination)
                }
                R.id.bnav_shortlists -> {
                    val destination = PropertiesListFragment()
                    destination.arguments = Bundle().apply {
                        putBoolean(PropertiesListFragment.ONLY_SHORTLISTED_KEY, true)
                        putBoolean(PropertiesListFragment.HIDE_TOOLBAR_KEY, true)
                    }
                    programmaticNavSelection?.let {
                         if (it.extraArgs != null) destination.arguments?.putAll(it.extraArgs)
                        loadFragmentInternal(destination, it.pushToBackStack)
                    } ?: loadFragmentInternal(destination)
                }
                R.id.bnav_listings -> {
                    val destination = ListingsFragment()
                    programmaticNavSelection?.let {
                        destination.arguments = it.extraArgs
                        loadFragmentInternal(destination, it.pushToBackStack)
                    } ?: loadFragmentInternal(destination)
                }
                R.id.bnav_profile -> {
                    val destination = ProfileFragment()
                    programmaticNavSelection?.let {
                        destination.arguments = it.extraArgs
                        loadFragmentInternal(destination, it.pushToBackStack)
                    } ?: loadFragmentInternal(destination)
                }
            }

            programmaticNavSelection = null // MAKE IT NULL
            return@setOnItemSelectedListener true
        }

        binding.bottomNavigation.setOnItemReselectedListener { item ->
            logInfo("<------ setOnItemReselectedListener ------->")
        }
    }

    override fun showBottomNav() {
        binding.bottomNavigationContainer.visibility = View.VISIBLE
    }

    override fun hideBottomNav() {
        binding.bottomNavigationContainer.visibility = View.GONE
    }

    private fun observeViewModel() {
        sharedDataViewModel.logOutUser.observe(this) { shouldLogOut ->
            if (shouldLogOut) {
                // LOG OUT
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
                finish()
                showToast("Logged out successfully.")
            }
        }

        preferredThemeViewModel.isThemeUpdated.observe(this) {
            if (it) {
                preferredThemeViewModel.clearThemUpdate()
                recreate() // Recreate To Load Current Theme
            }
        }
    }

    fun loadFragmentInternal(
        fragment: Fragment,
        pushToBackStack: Boolean = false,
        removeHistory: Boolean = false,
        containerId: Int = binding.pageFragmentContainer.id
    ) {
        loadFragment(fragment, containerId, pushToBackStack, removeHistory)
    }

    override fun navigateTo(destination: NavigationDestination) {
        when(destination) {
            is NavigationDestination.CreateProperty,
            is NavigationDestination.PropertyList,
            is NavigationDestination.SeparateSearch,
            is NavigationDestination.ProfileEdit, -> {
                val fragment = destination.fragmentClass.getDeclaredConstructor().newInstance()
                fragment.arguments = destination.args

                loadFragmentInternal(
                    fragment,
                    destination.pushToBackStack,
                    destination.removeExistingHistory,
                )
            }
            is NavigationDestination.MultipleImages,
            is NavigationDestination.InPlaceSearch,
            is NavigationDestination.SinglePropertyDetails,
            is NavigationDestination.EditProperty -> {
                val fragment = destination.fragmentClass.getDeclaredConstructor().newInstance()
                fragment.arguments = destination.args

                addFragment(
                    fragment,
                    binding.pageFragmentContainer.id,
                    destination.pushToBackStack,
                    destination.removeExistingHistory,
                )
            }
            is NavigationDestination.MyLeads,
            is NavigationDestination.MyProperties -> {
                programmaticNavSelection = ProgrammaticNavSelection(destination.args)
                binding.bottomNavigation.selectedItemId = R.id.bnav_listings
            }
            is NavigationDestination.ShortlistedProperties -> {
                programmaticNavSelection = ProgrammaticNavSelection(destination.args)
                binding.bottomNavigation.selectedItemId = R.id.bnav_shortlists
            }
        }
    }

    override fun navigateBack() {
        if (supportFragmentManager.backStackEntryCount != 0) {
            // If something added into backstack don't interfere
            supportFragmentManager.popBackStack()
            return
        }

        with(binding) {
            when (bottomNavigation.selectedItemId) {
                R.id.bnav_home -> {
                    finish() // FINISH THE CURRENT ACTIVITY
                }
                R.id.bnav_shortlists,
                R.id.bnav_listings,
                R.id.bnav_profile -> {
                    programmaticNavSelection = ProgrammaticNavSelection()
                    bottomNavigation.selectedItemId = R.id.bnav_home  // NAVIGATE TO HOME
                }

            }
        }
    }


    override fun navigateToRoot() {
        when (binding.bottomNavigation.selectedItemId) {
            R.id.bnav_home -> {
                finish() // FINISH THE CURRENT ACTIVITY
            }
            R.id.bnav_shortlists,
            R.id.bnav_listings,
            R.id.bnav_profile -> {
                programmaticNavSelection = ProgrammaticNavSelection()
                binding.bottomNavigation.selectedItemId = R.id.bnav_home  // NAVIGATE TO HOME
            }
        }
    }

    companion object {
        const val CURRENT_USER_KEY = "currentUser"
    }
}