package com.example.houserentalapp.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.ActivityMainBinding
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.presentation.enums.FragmentAnimationType
import com.example.houserentalapp.presentation.ui.auth.AuthActivity
import com.example.houserentalapp.presentation.ui.home.HomeFragment
import com.example.houserentalapp.presentation.ui.interfaces.BottomNavController
import com.example.houserentalapp.presentation.ui.interfaces.FragmentNavigationHandler
import com.example.houserentalapp.presentation.ui.listings.ListingsFragment
import com.example.houserentalapp.presentation.ui.profile.ProfileFragment
import com.example.houserentalapp.presentation.ui.property.CreatePropertyActivity
import com.example.houserentalapp.presentation.ui.property.PropertiesListFragment
import com.example.houserentalapp.presentation.ui.sharedviewmodel.PreferredThemeViewModel
import com.example.houserentalapp.presentation.ui.sharedviewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.ui.sharedviewmodel.SharedDataViewModelFactory
import com.example.houserentalapp.presentation.utils.extensions.addFragment
import com.example.houserentalapp.presentation.utils.extensions.loadFragment
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.logWarning
import com.example.houserentalapp.presentation.utils.extensions.showToast
import com.example.houserentalapp.presentation.utils.helpers.convertToBundle

/* TODO:
        Existing fix:
         7. Fix Fonts in every place
         8. Improve alert dialog design
        New:
         1. Favourites page -> move and remove properties
*/

class MainActivity : AppCompatActivity(), BottomNavController, FragmentNavigationHandler {
    private lateinit var binding: ActivityMainBinding
    val sharedDataViewModel: SharedDataViewModel by viewModels(null) {
        SharedDataViewModelFactory(this.applicationContext)
    }
    val preferredThemeViewModel: PreferredThemeViewModel by viewModels()
    private var currentUserID = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        preferredThemeViewModel.getTheme()?.let { setTheme(it.theme) } // Set Theme

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setWindowInsets()
        observeViewModel()

        // Set Current User Into Shared ViewModel // TODO - ONLY RECEIVE USER ID
        currentUserID = intent.getLongExtra(BundleKeys.CURRENT_USER_ID, 0L)
        if (currentUserID == 0L){
            logError("Current User is not found in intent")
            onUserLoadFailed()
            return
        }

        setBottomNavigation()
        if (savedInstanceState == null) {
            sharedDataViewModel.loadCurrentUser(currentUserID) { isSuccess ->
                if (!isSuccess) {
                    onUserLoadFailed()
                    return@loadCurrentUser
                }
                loadFragmentInternal(HomeFragment()) // Initial Render
                isNavigationFromShortcuts(intent) // Further render if any
            }
        }
    }

    private fun onUserLoadFailed() {
        startActivity(Intent(this, AuthActivity::class.java))
        showToast("Unknown error while fetching user data")
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        isNavigationFromShortcuts(intent)
    }

    private fun isNavigationFromShortcuts(intent: Intent): Boolean {
        val destinationPage = intent.getStringExtra(BundleKeys.DESTINATION_PAGE) ?: run {
            logDebug("isNavigationFromShortcuts: No destinationPage found")
            return false
        }

        when(destinationPage) {
            "searchProperty" -> navigateTo(NavigationDestination.SeparateSearch())
            "createProperty" -> navigateTo(NavigationDestination.CreateProperty())
            "leads" -> navigateTo(NavigationDestination.MyLeads())
            "singlePropertyDetail" -> {
                // NOTE: Android is converting the nested bundle into PersistableBundle
                val persistable = intent.getParcelableExtra<PersistableBundle>(BundleKeys.BUNDLE) ?: run {
                    logWarning("no bundle found")
                    return false
                }
                navigateTo(NavigationDestination.SinglePropertyDetail(
                    convertToBundle(persistable)
                ))
            }
            else -> {
                logWarning("destinationPage: $destinationPage isn't matched with any pages")
                return false
            }
        }

        return true
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

    private fun addDynamicShortCut(propertyName: String, bundle: Bundle?) {
        val shortcutId = "lastSeenProperty"
        val shortcuts = ShortcutManagerCompat.getDynamicShortcuts(applicationContext)
        // Check if already added
        shortcuts.find { it.id == shortcutId }?.let {
            logDebug("Shortcut already exists")
            return
        }

        // Remove old dynamic shortcut
        if (shortcuts.isNotEmpty())
            ShortcutManagerCompat.removeDynamicShortcuts(
                applicationContext, shortcuts.map { it.id }
            )

        // Add New ShortCut
        val shortcut = ShortcutInfoCompat.Builder(applicationContext, shortcutId)
            .setShortLabel(propertyName)
            .setIcon(IconCompat.createWithResource(applicationContext, R.drawable.bottom_nav_home_icon))
            .setIntent(
                // Even if the NotesHomePage is active clicking on shortcut opening it again newly
                Intent(applicationContext, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra(BundleKeys.BUNDLE, bundle)
                    putExtra(BundleKeys.DESTINATION_PAGE, "singlePropertyDetail")
                }
            )
            .build()

        logInfo("New Dynamic Shortcut added.")
        ShortcutManagerCompat.pushDynamicShortcut(applicationContext, shortcut)
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
                        putBoolean(BundleKeys.ONLY_SHORTLISTED, true)
                        putBoolean(BundleKeys.HIDE_TOOLBAR, true)
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
        binding.bottomNavigationContainer.postDelayed(
            100,
            {binding.bottomNavigationContainer.visibility = View.GONE}
        )
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
        containerId: Int = binding.pageFragmentContainer.id,
        animationType: FragmentAnimationType? = null
    ) {
        loadFragment(fragment, containerId, pushToBackStack, removeHistory, animationType)
    }

    override fun navigateTo(destination: NavigationDestination) {
        when(destination) {
            is NavigationDestination.CreateProperty -> {
                if (destination.activityClass != null) {
                    val intent = Intent(this, destination.activityClass)
                    intent.putExtra(BundleKeys.CURRENT_USER_ID, currentUserID)
                    startActivity(intent)
                }
            }
            is NavigationDestination.EditProperty -> {
                if (destination.fragmentClass != null) {
                    val fragment = destination.fragmentClass
                        .getDeclaredConstructor()
                        .newInstance().apply { arguments = destination.args }
                    loadFragmentInternal(
                        fragment,
                        destination.pushToBackStack,
                        destination.removeExistingHistory,
                    )
                    hideBottomNav()
                }
            }
            is NavigationDestination.PropertyList,
            is NavigationDestination.SeparateSearch,
            is NavigationDestination.RecommendedSinglePropertyDetail,
            is NavigationDestination.ProfileEdit, -> {
                if (destination.fragmentClass != null) {
                    val fragment = destination.fragmentClass
                        .getDeclaredConstructor()
                        .newInstance().apply { arguments = destination.args }
                    loadFragmentInternal(
                        fragment,
                        destination.pushToBackStack,
                        destination.removeExistingHistory,
                    )
                }
            }
            is NavigationDestination.MultipleImages,
            is NavigationDestination.InPlaceSearch -> {
                if (destination.fragmentClass != null) {
                    val fragment = destination.fragmentClass
                        .getDeclaredConstructor()
                        .newInstance().apply { arguments = destination.args }
                    addFragment(
                        fragment,
                        binding.pageFragmentContainer.id,
                        destination.pushToBackStack,
                        destination.removeExistingHistory,
                    )
                }
            }
            is NavigationDestination.SinglePropertyDetail -> {
                if (destination.fragmentClass != null) {
                    val fragment = destination.fragmentClass
                        .getDeclaredConstructor()
                        .newInstance().apply { arguments = destination.args }
                    if (destination.args == null) {
                        logWarning("To navigate SinglePropertyDetail arguments are mandatory")
                        return
                    }

                    if (destination.args.getBoolean(BundleKeys.IS_TENANT_VIEW))
                    // Add Dynamic Shortcut
                        addDynamicShortCut("Last Seen Property", destination.args)

                    addFragment(
                        fragment,
                        binding.pageFragmentContainer.id,
                        destination.pushToBackStack,
                        destination.removeExistingHistory,
                    )
                }
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
}