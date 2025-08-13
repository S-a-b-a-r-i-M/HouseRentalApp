package com.example.houserentalapp.presentation.ui

import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.ActivityStarterBinding
import com.example.houserentalapp.presentation.ui.home.HomeFragment
import com.example.houserentalapp.presentation.ui.listings.ListingsFragment
import com.example.houserentalapp.presentation.ui.profile.ProfileFragment
import com.example.houserentalapp.presentation.ui.shortlisted.ShortlistsFragment
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.showToast

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
        binding = ActivityStarterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setWindowInsets()

//        onBackPressedDispatcher.addCallback(backPressedCallback)

        setBottomNavigation()
        if (savedInstanceState == null)
            loadFragment(HomeFragment())
    }

    private fun setWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.starter)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
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