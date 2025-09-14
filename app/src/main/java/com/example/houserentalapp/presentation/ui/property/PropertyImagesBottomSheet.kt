package com.example.houserentalapp.presentation.ui.property

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentPropertyImagesBottomSheetBinding
import com.example.houserentalapp.presentation.ui.MainActivity
import com.example.houserentalapp.presentation.ui.property.adapter.PropertyImagesEditAdapter
import com.example.houserentalapp.presentation.ui.property.viewmodel.CreatePropertyViewModel
import com.example.houserentalapp.presentation.utils.extensions.createPropertyViewModelFactory
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.showToast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File

class PropertyImagesBottomSheet : BottomSheetDialogFragment(
    R.layout.fragment_property_images_bottom_sheet
) {
    private lateinit var binding: FragmentPropertyImagesBottomSheetBinding
    private lateinit var imagesAdapter: PropertyImagesEditAdapter
    private lateinit var mainActivity: MainActivity
    private val viewModel: CreatePropertyViewModel by activityViewModels {
        createPropertyViewModelFactory()
    }

    private lateinit var photoUri: Uri
    private val openCameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success){
            logInfo("got image $photoUri")
            viewModel.addPropertyImage(photoUri)
            showAnotherPhotoDialog()
        }
    }

    private val imagesPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { it ->
        if (it.resultCode == RESULT_OK && it.data != null) {
            val clipData = it.data?.clipData
            val imageUris = mutableListOf<Uri>()

            if (clipData != null)
            // Multiple images selected
                for (i in 0 until clipData.itemCount)
                    imageUris.add(clipData.getItemAt(i).uri)
            else
            // Single image selected
                it.data?.data?.let { uri ->  imageUris.add(uri) }

            if (imageUris.isNotEmpty()) {
                viewModel.addPropertyImages(imageUris)
                requireActivity().showToast("${imageUris.size} selected successfully")
            }
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
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
        binding = FragmentPropertyImagesBottomSheetBinding.bind(view)

        setupRecyclerView()
        observeViewModel()
        setClickListeners()
    }

    fun setupRecyclerView() {
        imagesAdapter = PropertyImagesEditAdapter {
            viewModel.removePropertyImage(it)
        }
        binding.rvImages.apply {
            adapter = imagesAdapter
            layoutManager = GridLayoutManager(context, 2)
        }
    }

    private fun observeViewModel() {
        viewModel.images.observe(viewLifecycleOwner) { imagesAdapter.setDataList(it) }
    }

    private fun openImagePicker() {
        // Without Permission also it's working
        // action will tell what exactly we are intent to do.
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) //
        }
        imagesPickerLauncher.launch(intent)
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

    private fun checkCameraPermissionAndOpenCamera() {
        // Camera Permission
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        )
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        else
            openCamera()
    }

    private fun showAnotherPhotoDialog() {
        AlertDialog.Builder(mainActivity)
            .setMessage("Take another photo ?")
            .setPositiveButton("Yes") {_, _ -> checkCameraPermissionAndOpenCamera() }
            .setNegativeButton("Done") {_, _ -> }
            .show()
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

    private fun setClickListeners() {
        with(binding) {
            btnClose.setOnClickListener {
                dismiss()
            }

            btnAddMoreImages.setOnClickListener {
                showAddImageOptions()
            }
        }
    }
}