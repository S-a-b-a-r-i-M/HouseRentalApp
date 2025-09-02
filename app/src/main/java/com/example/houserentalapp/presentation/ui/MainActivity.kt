package com.example.houserentalapp.presentation.ui

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.ActivityMainBinding
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.presentation.ui.home.HomeFragment
import com.example.houserentalapp.presentation.ui.listings.ListingsFragment
import com.example.houserentalapp.presentation.ui.profile.ProfileFragment
import com.example.houserentalapp.presentation.ui.property.PropertiesListFragment
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.showToast
import com.example.houserentalapp.presentation.utils.extensions.simpleClassName

/* Pending Things
    Existing fix:
        1. TODO: Create Property -> images(add more, storage) , enhance the counter view design
    New:
        1. TODO: Favourites page -> move and remove properties
        2. TODO: Filters
        2. TODO: My Properties Page -> List of uploaded properties, edit status, edit details.
 */

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var currentUser: User

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
            id = 2,
            name = "Tenant",
            phone = "9988776655",
            email = "tenent@gmail.com",
            createdAt = 123465689L
        )

//        onBackPressedDispatcher.addCallback(backPressedCallback)

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
                    val destinationFragment = PropertiesListFragment()
                    destinationFragment.arguments = Bundle().apply {
                        putBoolean(PropertiesListFragment.HIDE_BOTTOM_NAV_KEY, false)
                        putBoolean(PropertiesListFragment.ONLY_SHORTLISTED_KEY, true)
                        putBoolean(PropertiesListFragment.HIDE_TOOLBAR_KEY, true)
                        putBoolean(PropertiesListFragment.HIDE_FAB_BUTTON_KEY, true)
                    }
                    loadFragment(destinationFragment)
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
        containerId: Int = binding.pageFragmentContainer.id
    ) {
        // EXISTING FRAGMENT
        val existingFragment = supportFragmentManager.findFragmentById(containerId)

        supportFragmentManager.beginTransaction().apply {
            add(containerId, fragment)
            if (pushToBackStack) addToBackStack(fragment.simpleClassName) // ADDING THE CURRENT FRAGMENT/ACTIVITY INTO THE BACKSTACK
            commit()
        }
    }

    fun showBottomNav() {
        binding.bottomNavigationContainer.visibility = View.VISIBLE
    }

    fun hideBottomNav() {
        binding.bottomNavigationContainer.visibility = View.GONE
    }

    companion object {
        const val CURRENT_USER_ID_KEY = "currentUserId"
    }
}