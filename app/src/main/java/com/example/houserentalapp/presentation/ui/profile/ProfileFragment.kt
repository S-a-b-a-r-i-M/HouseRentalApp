package com.example.houserentalapp.presentation.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.houserentalapp.R
import com.example.houserentalapp.data.repo.UserRepoImpl
import com.example.houserentalapp.databinding.FragmentProfileBinding
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.usecase.UserUseCase
import com.example.houserentalapp.presentation.ui.MainActivity
import com.example.houserentalapp.presentation.ui.auth.AuthActivity
import com.example.houserentalapp.presentation.ui.profile.viewmodel.ProfileViewModel
import com.example.houserentalapp.presentation.ui.profile.viewmodel.ProfileViewModelFactory
import com.example.houserentalapp.presentation.ui.property.viewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.openDialer
import com.example.houserentalapp.presentation.utils.extensions.openMail
import com.example.houserentalapp.presentation.utils.extensions.showToast
import com.example.houserentalapp.presentation.utils.helpers.getTimePeriod
import com.example.houserentalapp.presentation.utils.helpers.loadImageSourceToImageView
import com.example.houserentalapp.presentation.ui.components.showImageDialog
import kotlin.getValue


class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var mainActivity: MainActivity
    private lateinit var currentUser: User
    private lateinit var viewModel: ProfileViewModel
    private val sharedDataViewModel: SharedDataViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)

        setupUI()
        setupViewModel()
        setupListeners()
        setupObservers()
    }

    private fun setupUI() {
        // Show Bottom Nav
        mainActivity.showBottomNav()

        with(binding) {
            tvAdminEmail.text = getString(R.string.admin_gmail_com)
            tvAdminPhone.text = getString(R.string._9987654321)
        }
    }

    private fun setupViewModel() {
        val repo = UserRepoImpl(mainActivity)
        val factory = ProfileViewModelFactory(UserUseCase(repo))
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class]
    }

    private fun logout() {
        viewModel.logOutCurrentUser(currentUser.id) {
            val intent = Intent(mainActivity, AuthActivity::class.java)
            startActivity(intent)
            mainActivity.finish()
            mainActivity.showToast("Logged out successfully.")
        }
    }

    private fun onLogoutClicked() {
        AlertDialog.Builder(mainActivity)
            .setIcon(R.drawable.baseline_logout_24)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupListeners() {
        with(binding) {
            toolbar.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.tbar_logout) {
                    logInfo("Logout User")
                    onLogoutClicked()
                    return@setOnMenuItemClickListener true
                }

                false
            }

            tvEditProfile.setOnClickListener {
                mainActivity.addFragment(ProfileEditFragment(), true)
            }

            imgUserProfile.setOnClickListener {
                currentUser.profileImageSource?.let {
                    requireContext().showImageDialog(it)
                }
            }

            ibtnDialAdmin.setOnClickListener { mainActivity.openDialer(tvAdminPhone.text.toString()) }
            ibtnEmailAdmin.setOnClickListener { mainActivity.openMail(tvAdminEmail.text.toString()) }
        }
    }

    private fun bindUserData(user: User) {
        with(binding) {
            tvWelcome.text = System.currentTimeMillis().getTimePeriod() + " ${user.name}"
            tvUserEmail.text = user.email ?: "No Email"
            tvUserPhone.text = user.phone
            if (user.profileImageSource != null) {
                imgUserProfile.isClickable = true
                imgUserProfile.isEnabled = true
                loadImageSourceToImageView(user.profileImageSource, imgUserProfile)
            }
            else {
                imgUserProfile.setImageDrawable(
                    mainActivity.getDrawable(R.drawable.bottom_nav_profile_icon)
                )
                imgUserProfile.isClickable = false
                imgUserProfile.isEnabled = false
            }
        }
    }

    private fun setupObservers() {
        sharedDataViewModel.currentUserLD.observe(mainActivity) {
            if (it != null) {
                currentUser = it
                bindUserData(it)
            }
        }
    }
}