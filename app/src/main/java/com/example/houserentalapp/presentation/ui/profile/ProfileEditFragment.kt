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
import com.example.houserentalapp.domain.usecase.UserUseCase
import com.example.houserentalapp.presentation.ui.MainActivity
import com.example.houserentalapp.presentation.ui.components.showImageDialog
import com.example.houserentalapp.presentation.ui.profile.viewmodel.ProfileEditViewModel
import com.example.houserentalapp.presentation.ui.profile.viewmodel.ProfileEditViewModelFactory
import com.example.houserentalapp.presentation.ui.profile.viewmodel.UserEditFormField
import com.example.houserentalapp.presentation.ui.property.viewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.showToast
import com.example.houserentalapp.presentation.utils.helpers.ImageUploadHelper
import com.example.houserentalapp.presentation.utils.helpers.loadImageSourceToImageView
import com.google.android.material.textfield.TextInputLayout

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
            profileEditViewModel.updateChanges(UserEditFormField.PROFILE_IMAGE, uri)
        }
    }

    private fun handleCameraResult(uri: Uri) {
        logInfo("got image $uri")
        profileEditViewModel.updateChanges(UserEditFormField.PROFILE_IMAGE, uri)
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
            toolbar.setNavigationOnClickListener { handleOnBackNavigation() }

            listOf(
                Pair(etName, UserEditFormField.NAME),
                Pair(etEmail, UserEditFormField.EMAIL),
            ).forEach { (et, field) ->
                et.addTextChangedListener {
                    profileEditViewModel.updateChanges(field, et.text.toString().trim())
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

            imgUserProfile.setOnClickListener {
                profileEditViewModel.editableUser.value!!.profileImageSource?.let {
                    requireContext().showImageDialog(it)
                }
            }
        }
    }

    private fun requestFocus() {
        profileEditViewModel.getFieldError(UserEditFormField.NAME).value?.let {
            binding.etName.requestFocus()
            binding.scrollView.smoothScrollTo(0, binding.etName.top)
            return
        }

        profileEditViewModel.getFieldError(UserEditFormField.EMAIL).value?.let {
            binding.etEmail.requestFocus()
            binding.scrollView.smoothScrollTo(0, binding.etEmail.top)
            return
        }
    }

    private fun observeValidationError(field: UserEditFormField, inputLayout: TextInputLayout) {
        profileEditViewModel.getFieldError(field).observe(viewLifecycleOwner) {
            inputLayout.error = it
        }
    }

    private fun setupObservers() {
        with(profileEditViewModel) {
            isFormDirty.observe(viewLifecycleOwner) { isFormDirty ->
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

            editableUser.observe(viewLifecycleOwner) {
                if (it.profileImageSource != null)
                    loadImageSourceToImageView(it.profileImageSource, binding.imgUserProfile)
            }

            observeValidationError(UserEditFormField.NAME, binding.tilName)
            observeValidationError(UserEditFormField.EMAIL, binding.tilEmail)

            validationError.observe(viewLifecycleOwner) {
                if (it != null) {
                    requestFocus()
                    clearValidationError()
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        mainActivity.showBottomNav()
    }
}