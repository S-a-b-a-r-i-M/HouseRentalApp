package com.example.houserentalapp.presentation.ui.auth

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentSignInBinding
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import android.view.animation.AnimationUtils
import androidx.lifecycle.ViewModelProvider
import com.example.houserentalapp.data.repo.UserRepoImpl
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.usecase.UserUseCase
import com.example.houserentalapp.presentation.ui.auth.viewmodel.SignInViewModel
import com.example.houserentalapp.presentation.ui.auth.viewmodel.SignInViewModelFactory
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.showToast
import com.example.houserentalapp.presentation.utils.helpers.validatePasswordStrength
import com.example.houserentalapp.presentation.utils.helpers.validatePhoneFormat

class SignInFragment : Fragment(R.layout.fragment_sign_in) {
    private lateinit var binding: FragmentSignInBinding
    private lateinit var authActivity: AuthActivity
    private lateinit var viewModel: SignInViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        authActivity = context as AuthActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSignInBinding.bind(view)

        setupUI()
        setupViewModel()
        setupListener()
        setupObserver()
    }

    private fun setupUI() {

    }

    private fun setupViewModel() {
        val repo = UserRepoImpl(authActivity)
        val factory = SignInViewModelFactory(UserUseCase(repo))
        viewModel = ViewModelProvider(this, factory)[SignInViewModel::class]
    }

    private fun areFieldsValid() = with(binding) {
        var isValid = true
        // Phone
        validatePhoneFormat(etPhone.text.toString())?.let {
            etPhone.requestFocus() // TODO: How auto scrolling happens
            isValid = false
            tilPhone.error = it
        }

        // Password
        validatePasswordStrength(etPassword.text.toString())?.let {
            if (isValid) etPassword.requestFocus()
            isValid = false
            tilPassword.error = it
        }

        isValid
    }

    private fun onSignInClicked() {
        if (!areFieldsValid()) {
            showError("Please fix the errors above")
            return
        }

        viewModel.signIn(
            binding.etPhone.text.toString(),
            binding.etPassword.text.toString()
        )
    }

    private fun setupListener() {
        with(binding) {
            ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
                val isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())

                if (isKeyboardVisible) {
                    logInfo("Keyboard is visible")
                    tvTitle.visibility = View.INVISIBLE
                } else {
                    logInfo("Keyboard is hidden")
                    tvTitle.startAnimation(
                        AnimationUtils.loadAnimation(tvTitle.context, R.anim.fade_in)
                    )
                    tvTitle.visibility = View.VISIBLE
                }
                insets
            }

            tvSignUp.setOnClickListener {
                authActivity.loadFragment(SignUpFragment(), true)
            }

            btnSignIn.setOnClickListener { onSignInClicked() }
        }
    }

    private fun setupObserver() {
        viewModel.signInResult.observe(viewLifecycleOwner) {
            when(it) {
                is ResultUI.Success<User> -> {
                    stopLoading()
                    authActivity.navigateToMain(it.data)
                }
                is ResultUI.Error -> {
                    stopLoading()
                    showError(it.message)
                }
                ResultUI.Loading -> {
                    showLoading()
                }
            }
        }
    }

    private fun showLoading() {
        binding.btnSignIn.text = getString(R.string.signing_in)
    }

    private fun stopLoading() {
        binding.btnSignIn.postDelayed(
            { binding.btnSignIn.text = getString(R.string.sign_in) },
            50
        )
    }

    private fun showError(error: String) {
        authActivity.showToast(error)
    }
}