package com.example.houserentalapp.presentation.ui.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.houserentalapp.R
import com.example.houserentalapp.data.repo.UserRepoImpl
import com.example.houserentalapp.databinding.FragmentProfileEditBinding
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.model.enums.UserField
import com.example.houserentalapp.domain.usecase.UserUseCase
import com.example.houserentalapp.presentation.ui.MainActivity
import com.example.houserentalapp.presentation.ui.profile.viewmodel.ProfileEditViewModel
import com.example.houserentalapp.presentation.ui.profile.viewmodel.ProfileEditViewModelFactory
import com.example.houserentalapp.presentation.ui.property.viewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.showToast
import com.example.houserentalapp.presentation.utils.helpers.ImageUploadHelper
import com.example.houserentalapp.presentation.utils.helpers.loadImageSourceToImageView

class ProfileEditFragment : Fragment(R.layout.fragment_profile_edit) {
    private lateinit var binding: FragmentProfileEditBinding
    private lateinit var mainActivity: MainActivity
    private lateinit var currentUser: User
    private lateinit var imageHelper: ImageUploadHelper

    private val sharedDataViewModel: SharedDataViewModel by activityViewModels()
    private lateinit var profileEditViewModel: ProfileEditViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileEditBinding.bind(view)
        // Take Current User
        currentUser = sharedDataViewModel.currentUserData

        setupUI()
        setupViewModel()
        setupListeners()
        setupObservers()
        addBackPressCallBack()

        imageHelper = ImageUploadHelper().init(
            this,
            onImageFromCamera = ::handleCameraResult,
            onImageFromPicker = ::handleImagePrickerResult,
            onPermissionDenied = ::onCameraPermissionDenied
        )
    }

    private fun addBackPressCallBack() {
        mainActivity.onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleOnBackNavigation()
                }
            })
    }

    private fun handleImagePrickerResult(intent: Intent) {
        intent.data?.let { uri ->
            logInfo("Image Uploaded Successfully")
            profileEditViewModel.updateChanges(UserField.PROFILE_IMAGE, uri)
        }
    }

    private fun handleCameraResult(uri: Uri) {
        logInfo("got image $uri")
        profileEditViewModel.updateChanges(UserField.PROFILE_IMAGE, uri)
    }

    private fun onCameraPermissionDenied() {
        logInfo("User denied the camera permission")
        mainActivity.showToast("Please provide camera permission to take pictures")
    }

    private fun handleOnBackNavigation() {
        if (profileEditViewModel.isFormDirty.value == true)
            AlertDialog.Builder(mainActivity)
                .setTitle("Discard Changes")
                .setMessage("Are you sure you want to discard the changes ?")
                .setPositiveButton("Discard") { _, _ ->
                    parentFragmentManager.popBackStack()
                }
                .setNegativeButton("Cancel", null)
                .show()
        else
            parentFragmentManager.popBackStack()
    }

    private fun setupUI() {
        // Hide Bottom Nav
        mainActivity.hideBottomNav()

        with(binding) {
            toolbar.setNavigationOnClickListener { handleOnBackNavigation() }
        }
    }

    private fun setupViewModel() {
        val userUC = UserUseCase(UserRepoImpl(mainActivity))
        val factory = ProfileEditViewModelFactory(currentUser, userUC)
        profileEditViewModel = ViewModelProvider(this, factory)[ProfileEditViewModel::class]

        if (profileEditViewModel.isFormDirty.value != true) {
            with(binding) {
                etName.setText(currentUser.name)
                etPhone.setText(currentUser.phone)
                etEmail.setText(currentUser.email)
                currentUser.profileImageSource?.let { imageSource ->
                    loadImageSourceToImageView(imageSource, imgUserProfile)
                }
            }
        }
    }

    private fun setupListeners() {
        with(binding) {
            listOf(
                Pair(etName, UserField.NAME),
                Pair(etPhone, UserField.PHONE),
                Pair(etEmail, UserField.EMAIL),
            ).forEach { (et, field) ->
                et.addTextChangedListener {
                    profileEditViewModel.updateChanges(field, et.text.toString())
                }
            }

            tvChangeImage.setOnClickListener { imageHelper.showAddImageOptions(mainActivity) }

            btnSave.setOnClickListener {
                profileEditViewModel.saveUserChanges(
                    { updatedUser ->
                        sharedDataViewModel.setCurrentUser(updatedUser) // Update Modified User Into SharedViewModel
                        mainActivity.showToast("Changes saved successfully")
                        parentFragmentManager.popBackStack()
                    },
                    { errorMsg ->
                        mainActivity.showToast(errorMsg)
                    }
                )
            }
        }
    }

    private fun setupObservers() {
        profileEditViewModel.isFormDirty.observe(viewLifecycleOwner) { isFormDirty ->
            if (isFormDirty) // Only enable if form values changes
                binding.btnSave.apply {
                    isClickable = true
                    alpha = 1f
                }
            else
                binding.btnSave.apply {
                    isClickable = false
                    alpha = 0.2f
                }
        }

        profileEditViewModel.editableUser.observe(viewLifecycleOwner) {
            if(it.profileImageSource != null)
                loadImageSourceToImageView(it.profileImageSource, binding.imgUserProfile)
        }
    }

    override fun onDetach() {
        super.onDetach()
        mainActivity.showBottomNav()
    }
}