package com.example.houserentalapp.presentation.ui

import android.content.Intent
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
import com.example.houserentalapp.presentation.ui.property.viewmodel.SharedDataViewModel
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

    // TASK: IF USER CLICKS BACK BUTTON ON OTHER FRAGMENTS EXCEPT HOME WE HAVE TO NAVIGATE THEM TO HOME
    private val backPressedCallback = object : OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
            println("handleOnBackPressed ----> ")
            remove() // Remove on first click
            loadFragmentInternal(HomeFragment()) // Move to Home
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
        observeViewModel()

//        onBackPressedDispatcher.addCallback(backPressedCallback)

//        runBlocking {
//            UserRepoImpl(this@MainActivity).createUser(
//                "Sabari", "", "", ""
//            )
//        }

        setBottomNavigation()
        if (savedInstanceState == null)
            loadFragmentInternal(HomeFragment())
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
                    loadFragmentInternal(HomeFragment())
                }
                R.id.bnav_shortlists -> {
                    val destination = PropertiesListFragment()
                    destination.arguments = Bundle().apply {
                        putBoolean(PropertiesListFragment.ONLY_SHORTLISTED_KEY, true)
                        putBoolean(PropertiesListFragment.HIDE_TOOLBAR_KEY, true)
                    }
                    loadFragmentInternal(destination)
                }
                R.id.bnav_listings -> {
                    loadFragmentInternal(ListingsFragment())
                }
                R.id.bnav_profile -> {
                    loadFragmentInternal(ProfileFragment())
                }
            }

            return@setOnItemSelectedListener true
        }

        binding.bottomNavigation.setOnItemReselectedListener { item ->
            logInfo("setOnItemReselectedListener")
        }
    }

    override fun showBottomNav() {
        binding.bottomNavigationContainer.visibility = View.VISIBLE
    }

    override fun hideBottomNav() {
        binding.bottomNavigationContainer.visibility = View.GONE
    }

    fun loadFragmentInternal(
        fragment: Fragment,
        pushToBackStack: Boolean = false,
        removeHistory: Boolean = false,
        containerId: Int = binding.pageFragmentContainer.id
    ) {
        loadFragment(
            fragment,
            containerId,
            pushToBackStack,
            removeHistory
        )
    }

    override fun navigateTo(destination: NavigationDestination) {
        when(destination) {
            is NavigationDestination.CreateProperty,
            is NavigationDestination.PropertyList,
            is NavigationDestination.SeparateSearch,
            is NavigationDestination.ProfileEdit -> {
                val fragment = destination.fragmentClass.newInstance().apply { // TODO-DOOUT: getDeclaredConstructor()
                    arguments = destination.args
                }
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
                val fragment = destination.fragmentClass.getDeclaredConstructor().newInstance().apply {
                    arguments = destination.args
                }
                addFragment(
                    fragment,
                    binding.pageFragmentContainer.id,
                    destination.pushToBackStack,
                    destination.removeExistingHistory,
                )
            }
        }
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
    }

    companion object {
        const val CURRENT_USER_KEY = "currentUser"
    }
}