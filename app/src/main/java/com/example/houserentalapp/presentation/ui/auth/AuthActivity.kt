package com.example.houserentalapp.presentation.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.ActivityAuthBinding
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.presentation.ui.BundleKeys
import com.example.houserentalapp.presentation.ui.MainActivity
import com.example.houserentalapp.presentation.ui.MyAppApplication
import com.example.houserentalapp.presentation.ui.auth.viewmodel.AuthViewModel
import com.example.houserentalapp.presentation.ui.auth.viewmodel.AuthViewModelFactory
import com.example.houserentalapp.presentation.ui.sharedviewmodel.PreferredThemeViewModel
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.extensions.simpleClassName
import kotlin.getValue

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    lateinit var authViewModel: AuthViewModel
    val preferredThemeViewModel: PreferredThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
         preferredThemeViewModel.getTheme()?.let { setTheme(it.theme) } // Set Theme
//         val splashScreen = installSplashScreen() // Install splash screen

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setupViewModel()
        /*
        splashScreen.setKeepOnScreenCondition {
            // This lambda called ~60 times per second (every frame)
            logDebug("SplashScreen condition is invoked: ${authViewModel.isLoading}")
            authViewModel.isLoading // NOTE: It doesn't have to be mutable live data
        }*/

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setWindowInsets()

        if (savedInstanceState == null)
            authViewModel.loadUserIfAlreadyAuthenticated(::navigateToMain) {
                loadFragment(SignInFragment())
            }
    }

    private fun setWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.auth)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            binding.statusBarOverlayView.layoutParams.height = systemBars.top // ADD SYSTEM BAR PADDING
            v.setPadding(systemBars.left, 0, systemBars.right, imeInsets.bottom)
            insets
        }
    }

    private fun setupViewModel() {
        val factory = AuthViewModelFactory((application as MyAppApplication).authDependencyStore)
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class]
    }

    fun navigateToMain(currentUser: User) {
        val destinationPage = this.intent.getStringExtra(BundleKeys.DESTINATION_PAGE) // ShortCut Click
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(BundleKeys.CURRENT_USER_ID, currentUser.id)
        intent.putExtra(BundleKeys.DESTINATION_PAGE, destinationPage)
        startActivity(intent)
        finish()
    }

    fun loadFragment(
        fragment: Fragment,
        pushToBackStack: Boolean = false,
        containerId: Int = binding.fragmentContainer.id
    ) {
        supportFragmentManager.beginTransaction().apply {
            replace(containerId, fragment)
            if (pushToBackStack) addToBackStack(fragment.simpleClassName) // ADDING THE CURRENT FRAGMENT/ACTIVITY INTO THE BACKSTACK
            commit()
        }
    }
}