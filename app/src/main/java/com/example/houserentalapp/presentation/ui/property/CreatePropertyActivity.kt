package com.example.houserentalapp.presentation.ui.property

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.ActivityCreatePropertyBinding
import com.example.houserentalapp.presentation.ui.BundleKeys
import com.example.houserentalapp.presentation.ui.NavigationDestination
import com.example.houserentalapp.presentation.ui.ResultRequestKeys
import com.example.houserentalapp.presentation.ui.interfaces.FragmentNavigationHandler
import com.example.houserentalapp.presentation.ui.sharedviewmodel.PreferredThemeViewModel
import com.example.houserentalapp.presentation.utils.extensions.loadFragment
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.showToast

/*
    1. Fetch Current User Data From Singleton Application Context
*/

class CreatePropertyActivity : AppCompatActivity(), FragmentNavigationHandler {
    private lateinit var binding: ActivityCreatePropertyBinding
    private val preferredThemeViewModel: PreferredThemeViewModel by viewModels()
    private var currentUserId: Long = 0L
    private var isPropertyCreated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        preferredThemeViewModel.getTheme()?.let { setTheme(it.theme) } // Set Theme

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreatePropertyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setWindowInsets()

        currentUserId = intent.getLongExtra(BundleKeys.CURRENT_USER_ID, 0L)
        if (currentUserId == 0L) {
            logError("Current User ID not found.")
            showToast("Error occurred while fetching current user details.")
            finish() // Finish This Activity
        }

        setupUI()
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

    private fun setupUI() {
        addCreatePropertyResultListener()
        val destination = CreatePropertyFragment()
        destination.arguments = Bundle().apply { putLong(BundleKeys.CURRENT_USER_ID, currentUserId) }
        loadFragment(destination, binding.fragmentContainer.id)
    }

    private fun addCreatePropertyResultListener() {
        supportFragmentManager.setFragmentResultListener(
            ResultRequestKeys.PROPERTY_CREATED,
            this
        ) { requestKey, result ->
            isPropertyCreated = result.getBoolean(BundleKeys.IS_PROPERTY_CREATED)
        }
    }

    override fun navigateTo(destination: NavigationDestination) {
        TODO("Not yet implemented")
    }

    override fun navigateBack() {
        if (supportFragmentManager.backStackEntryCount != 0) {
            // If something added into backstack don't interfere
            supportFragmentManager.popBackStack()
            return
        }

        // OtherWise Finish This Activity
        setResult(
            if (isPropertyCreated) RESULT_OK else RESULT_CANCELED,
            Intent().apply {
                putExtra(BundleKeys.IS_PROPERTY_CREATED, isPropertyCreated)
            }
        )
        // Finish & Remove from task(recents)
        finishAndRemoveTask()
    }
}