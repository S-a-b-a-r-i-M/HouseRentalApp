package com.example.houserentalapp.presentation.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentProfileBinding
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.presentation.ui.MainActivity
import com.example.houserentalapp.presentation.ui.property.viewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.helpers.getTimePeriod
import kotlin.getValue


class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var mainActivity: MainActivity
    private val sharedDataViewModel: SharedDataViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)

        setupUI()
        setupListeners()
        setupObservers()
    }

    fun setupUI() {
        // Show Bottom Nav
        mainActivity.showBottomNav()

        with(binding) {
            tvAdminEmail.text = getString(R.string.andmin_gmail_com)
            tvAdminPhone.text = getString(R.string._9987654321)
        }
    }

    fun setupListeners() {
        with(binding) {
            toolbar.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.tbar_logout) {
                    logInfo("Logout User")
                    return@setOnMenuItemClickListener true
                }

                false
            }

            tvEditProfile.setOnClickListener {
                mainActivity.addFragment(ProfileEditFragment(), true)
            }

            ibtnDialAdmin.setOnClickListener { openDialer(tvAdminPhone.text.toString()) }
            ibtnEmailAdmin.setOnClickListener { openEmail(tvAdminEmail.text.toString()) }
        }
    }

    private fun openDialer(phone: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = "tel:$phone".toUri()
        }
        startActivity(intent)
    }

    private fun openEmail(email: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()  // ensures only email apps respond
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        }
        startActivity(intent)
    }

    private fun bindUserData(user: User) {
        with(binding) {
            tvWelcome.text = System.currentTimeMillis().getTimePeriod() + " ${user.name}"
            tvUserEmail.text = user.email
            tvUserPhone.text = user.phone
        }
    }

    fun setupObservers() {
        sharedDataViewModel.currentUser.observe(viewLifecycleOwner) {
            if (it != null) bindUserData(it)
        }
    }
}