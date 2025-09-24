package com.example.houserentalapp.presentation.ui.auth

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.houserentalapp.R
import com.example.houserentalapp.data.repo.UserRepoImpl
import com.example.houserentalapp.databinding.FragmentSignUpBinding
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.usecase.UserUseCase
import com.example.houserentalapp.presentation.model.NewUserUI
import com.example.houserentalapp.presentation.ui.auth.viewmodel.SignUpViewModel
import com.example.houserentalapp.presentation.ui.auth.viewmodel.SignUpViewModelFactory
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.showToast
import com.example.houserentalapp.presentation.utils.helpers.validatePasswordStrength
import com.example.houserentalapp.presentation.utils.helpers.validatePhoneFormat
import com.example.houserentalapp.presentation.utils.helpers.validateUserName
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SignUpFragment : Fragment(R.layout.fragment_sign_up) {
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var authActivity: AuthActivity
    private lateinit var viewModel: SignUpViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        authActivity = context as AuthActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSignUpBinding.bind(view)

        setupUI()
        setupViewModel()
        setupListener()
        setupObserver()
    }

    private fun setupUI() {

    }

    private fun setupViewModel() {
        val repo = UserRepoImpl(authActivity)
        val factory = SignUpViewModelFactory(UserUseCase(repo))
        viewModel = ViewModelProvider(this, factory)[SignUpViewModel::class]
    }

    private fun areFieldsValid() = with(binding) {
            var isValid = true
            // Name
            val userName = etName.text.toString().trim()
            validateUserName(userName)?.let {
                isValid = false
                tilName.error = it
                etName.requestFocus()
            }

            // Phone
            val phone = etPhone.text.toString().trim()
            validatePhoneFormat(phone)?.let {
                if (isValid) etPhone.requestFocus()
                isValid = false
                tilPhone.error = it
            }

            // Password
            val password = etPassword.text.toString().trim()
            validatePasswordStrength(password)?.let {
                if (isValid) etPassword.requestFocus()
                isValid = false
                tilPassword.error = it
            }

            isValid
        }

    private fun onSignUpClick() {
        if (!areFieldsValid()) {
            showError("Please fix the errors above")
            return
        }

        viewModel.signUp(
            NewUserUI(
                binding.etName.text.toString().trim(),
                binding.etPhone.text.toString().trim(),
                binding.etPassword.text.toString().trim()
            )
        )
    }

    private fun setTextChangedListener(et: TextInputEditText, til: TextInputLayout) {
        et.addTextChangedListener { if(til.error != null) til.error = null }
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

            floatingActionButton.setOnClickListener { parentFragmentManager.popBackStack() }

            tvSignIn.setOnClickListener { authActivity.loadFragment(SignInFragment()) }

            setTextChangedListener(etName, tilName)
            setTextChangedListener(etPhone, tilPhone)
            setTextChangedListener(etPassword, tilPassword)

            btnSignUp.setOnClickListener { onSignUpClick() }
        }
    }

    private fun setupObserver() {
        viewModel.signUpResult.observe(viewLifecycleOwner) {
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
        binding.btnSignUp.text = getString(R.string.signing_in)
    }

    private fun stopLoading() {
        binding.btnSignUp.postDelayed(
            { binding.btnSignUp.text = getString(R.string.sign_up) },
            50
        )
    }

    private fun showError(error: String) {
        authActivity.showToast(error)
    }
}