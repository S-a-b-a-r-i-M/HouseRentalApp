package com.example.houserentalapp.presentation.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.houserentalapp.R
import com.example.houserentalapp.data.repo.UserRepoImpl
import com.example.houserentalapp.databinding.ActivityAuthBinding
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.usecase.UserUseCase
import com.example.houserentalapp.presentation.ui.MainActivity
import com.example.houserentalapp.presentation.ui.auth.viewmodel.AuthViewModel
import com.example.houserentalapp.presentation.ui.auth.viewmodel.AuthViewModelFactory
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.simpleClassName

// TODO: HANDLE AUTH PORTION
class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setWindowInsets()

        setupViewModel()
        setupObservers()
        authViewModel.loadUserIfAlreadyAuthenticated()
    }

    private fun setWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.auth)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, imeInsets.bottom)
            insets
        }
    }

    private fun setupViewModel() {
        val repo = UserRepoImpl(this)
        val factory = AuthViewModelFactory(UserUseCase(repo))
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class]
    }

    private fun setupObservers() {
        authViewModel.currentUser.observe(this) {
            when(it) {
                is ResultUI.Success<User?> -> {
                    if (it.data != null)
                        navigateToMain(it.data)
                    else
                        loadFragment(SignInFragment())
                }
                is ResultUI.Error -> {
                    loadFragment(SignInFragment())
                }
                ResultUI.Loading -> {

                }
            }
        }
    }

    fun navigateToMain(currentUser: User) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(MainActivity.Companion.CURRENT_USER_KEY, currentUser)
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