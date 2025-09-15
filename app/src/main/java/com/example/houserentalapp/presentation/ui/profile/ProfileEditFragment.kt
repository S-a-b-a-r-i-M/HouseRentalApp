package com.example.houserentalapp.presentation.ui.profile

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
import com.example.houserentalapp.presentation.utils.helpers.loadImageSourceToImageView
import java.io.File

class ProfileEditFragment : Fragment(R.layout.fragment_profile_edit) {
    private lateinit var binding: FragmentProfileEditBinding
    private lateinit var mainActivity: MainActivity
    private lateinit var currentUser: User

    private val sharedDataViewModel: SharedDataViewModel by activityViewModels()
    private lateinit var profileEditViewModel: ProfileEditViewModel

    private val imagesPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK && it.data != null) {
                it.data?.data?.let { uri ->
                    logInfo("Image Uploaded Successfully")
                    profileEditViewModel.updateChanges(UserField.PROFILE_IMAGE, uri)
                }
        }
    }

    private lateinit var photoUri: Uri
    private val openCameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            logInfo("got image $photoUri")
            profileEditViewModel.updateChanges(UserField.PROFILE_IMAGE, photoUri)
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            logInfo("User approved the camera permission")
            openCamera()
        } else {
            logInfo("User denied the camera permission")
            mainActivity.showToast("Please provide camera permission to take pictures")
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileEditBinding.bind(view)
        // Take Current User
        currentUser = sharedDataViewModel.currentUserData ?: run {
            mainActivity.showToast("Login again...")
            mainActivity.finish()
            return
        }

        setupUI()
        setupViewModel()
        setupListeners()
        setupObservers()
        addBackPressCallBack()
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

    private fun showAddImageOptions() {
        val options = arrayOf("Camera", "Gallery")
        AlertDialog.Builder(mainActivity)
            .setTitle("Add Image")
            .setItems(options) { _, which ->
                when(which) {
                    0 -> checkCameraPermissionAndOpenCamera()
                    1 -> openImagePicker()
                }
            }
            .show()
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

            tvChangeImage.setOnClickListener { showAddImageOptions() }

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

        profileEditViewModel.profileImageSource.observe(viewLifecycleOwner) {
            if(it != null)
                loadImageSourceToImageView(it, binding.imgUserProfile)
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
        imagesPickerLauncher.launch(intent)
    }

    private fun checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        )
            permissionLauncher.launch(Manifest.permission.CAMERA)
        else
            openCamera()
    }

    private fun openCamera() {
        val photoFile = File.createTempFile("IMG_", ".jpg", mainActivity.cacheDir)
        photoUri = FileProvider.getUriForFile(
            mainActivity,
            mainActivity.packageName + ".provider",
            photoFile
        )
        openCameraLauncher.launch(photoUri)
    }

    override fun onDetach() {
        super.onDetach()
        mainActivity.showBottomNav()
    }
}