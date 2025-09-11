package com.example.houserentalapp.presentation.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

// TODO: HANDLE AUTH PORTION
class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setWindowInsets()

        setupViewModel()
        setupObservers()

        authViewModel.signIn("9878089777")
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
        val userUC = UserUseCase(UserRepoImpl(this))
        val factory = AuthViewModelFactory(userUC)
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class]
    }

    private fun setupObservers() {
        authViewModel.currentUser.observe(this) { user ->
            when(user) {
                is ResultUI.Success<User?> -> {
                    if (user.data == null) {
                        // show Error
                    }
                    else
                        navigateToMain(user.data)
                }
                is ResultUI.Error -> {

                }
                ResultUI.Loading -> {

                }
            }
        }
    }

    private fun navigateToMain(currentUser: User) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(MainActivity.Companion.CURRENT_USER_KEY, currentUser)
        startActivity(intent)
        finish()
    }
}