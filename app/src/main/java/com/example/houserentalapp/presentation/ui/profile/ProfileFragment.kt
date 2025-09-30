package com.example.houserentalapp.presentation.ui.profile

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentProfileBinding
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.presentation.enums.AppTheme
import com.example.houserentalapp.presentation.ui.NavigationDestination
import com.example.houserentalapp.presentation.ui.base.BaseFragment
import com.example.houserentalapp.presentation.ui.profile.viewmodel.ProfileViewModel
import com.example.houserentalapp.presentation.ui.profile.viewmodel.ProfileViewModelFactory
import com.example.houserentalapp.presentation.ui.sharedviewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.openDialer
import com.example.houserentalapp.presentation.utils.extensions.openMail
import com.example.houserentalapp.presentation.utils.helpers.getTimePeriod
import com.example.houserentalapp.presentation.utils.helpers.loadImageSourceToImageView
import com.example.houserentalapp.presentation.ui.components.showImageDialog
import com.example.houserentalapp.presentation.ui.interfaces.BottomNavController
import com.example.houserentalapp.presentation.ui.sharedviewmodel.PreferredThemeViewModel
import com.example.houserentalapp.presentation.utils.extensions.dpToPx
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.extensions.onBackPressedNavigateBack
import kotlin.getValue


class ProfileFragment : BaseFragment(R.layout.fragment_profile) {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var bottomNavController: BottomNavController
    private lateinit var currentUser: User
    private lateinit var viewModel: ProfileViewModel
    private val sharedDataViewModel: SharedDataViewModel by activityViewModels()
    val preferredThemeViewModel: PreferredThemeViewModel by activityViewModels()
    private val _context: Context get() = requireContext()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        bottomNavController = context as BottomNavController
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)

        setupUI()
        setupViewModel()
        setupListeners()
        setupObservers()
        onBackPressedNavigateBack()
    }

    private fun setupUI() {
        // Show Bottom Nav
        bottomNavController.showBottomNav()

        with(binding) {
            tvAdminEmail.text = getString(R.string.admin_gmail_com)
            tvAdminPhone.text = getString(R.string._9987654321)
        }
        buildThemeSwitcherOptions()
    }

    // Creating Drawable At Runtime
    private fun getShape(colorId: Int, withStroke: Boolean = false) = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(_context.getColor(colorId))
        if (withStroke) setStroke(1.dpToPx(), _context.getColor(R.color.black))
    }

    private fun buildThemeSwitcherOptions() {
        binding.viewBlue.background = getShape(R.color.primary_blue)
        binding.viewViolet.background = getShape(R.color.violet_primary)
        binding.viewGreen.background = getShape(R.color.green_primary)
        onPreferredThemeReceived(preferredThemeViewModel.getTheme())
    }

    private fun onPreferredThemeReceived(theme: AppTheme?) {
        val (view, colorId) = when(theme) {
            AppTheme.VIOLET -> {
                binding.viewBlue.isEnabled = true
                binding.viewGreen.isEnabled = true
                Pair(binding.viewViolet, R.color.violet_primary)
            }
            AppTheme.BLUE, null -> { // Default Theme
                binding.viewViolet.isEnabled = true
                binding.viewGreen.isEnabled = true
                Pair(binding.viewBlue, R.color.primary_blue)
            }
            AppTheme.GREEN -> {
                binding.viewBlue.isEnabled = true
                binding.viewViolet.isEnabled = true
                Pair(binding.viewGreen, R.color.green_primary)
            }
        }
        // Change Appearance
        view.apply {
            background = getShape(colorId, true)
            scaleX = 1.20f
            scaleY = 1.20f
            isEnabled = false
        }
    }

    private fun setupViewModel() {
        val factory = ProfileViewModelFactory(_context.applicationContext)
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class]
    }

    private fun logout() {
        viewModel.logOutCurrentUser(currentUser.id) { sharedDataViewModel.logOutUser() }
    }

    private fun onLogoutClicked() {
        AlertDialog.Builder(_context)
            .setIcon(R.drawable.baseline_logout_24)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveTheme(theme: AppTheme) {
        logDebug("New Theme Selected: ${theme.readable}")
        preferredThemeViewModel.saveTheme(theme)
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
                navigateTo(NavigationDestination.ProfileEdit())
            }

            imgUserProfile.setOnClickListener {
                currentUser.profileImageSource?.let {
                    requireContext().showImageDialog(it)
                }
            }

            viewBlue.setOnClickListener { saveTheme(AppTheme.BLUE) }

            viewViolet.setOnClickListener { saveTheme(AppTheme.VIOLET) }

            viewGreen.setOnClickListener { saveTheme(AppTheme.GREEN) }

            ibtnDialAdmin.setOnClickListener { _context.openDialer(tvAdminPhone.text.toString()) }
            ibtnEmailAdmin.setOnClickListener { _context.openMail(tvAdminEmail.text.toString()) }
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
                imgUserProfile.setImageResource(R.drawable.bottom_nav_profile_icon)
                imgUserProfile.isClickable = false
                imgUserProfile.isEnabled = false
            }
        }
    }

    private fun setupObservers() {
        sharedDataViewModel.currentUserLD.observe(viewLifecycleOwner) {
            if (it != null) {
                currentUser = it
                bindUserData(it)
            }
        }
    }
}