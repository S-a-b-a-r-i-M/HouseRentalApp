package com.example.houserentalapp.presentation.ui.property

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
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
import com.example.houserentalapp.presentation.utils.helpers.ImageUploadHelper
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PropertyImagesBottomSheet : BottomSheetDialogFragment(
    R.layout.fragment_property_images_bottom_sheet
) {
    private lateinit var binding: FragmentPropertyImagesBottomSheetBinding
    private lateinit var imagesAdapter: PropertyImagesEditAdapter
    private lateinit var mainActivity: MainActivity
    private lateinit var imageUploadHelper: ImageUploadHelper

    private val viewModel: CreatePropertyViewModel by activityViewModels {
        createPropertyViewModelFactory()
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
        setImageHelper()
    }

    private fun setImageHelper() {
        imageUploadHelper = ImageUploadHelper().init(
            this,
            onImageFromCamera = ::handleCameraResult,
            onImageFromPicker = ::handlePickerResult,
            onPermissionDenied = ::onCameraPermissionDenied,
        )
        imageUploadHelper.multipleImagesFromPicker = true
    }

    private fun handleCameraResult(uri: Uri) {
        logInfo("got image $uri")
        viewModel.addPropertyImage(uri)
        showAnotherPhotoDialog()
    }

    private fun handlePickerResult(intent: Intent) {
        val clipData = intent.clipData
        val imageUris = mutableListOf<Uri>()

        if (clipData != null)
        // Multiple images selected
            for (i in 0 until clipData.itemCount)
                imageUris.add(clipData.getItemAt(i).uri)
        else
        // Single image selected
            intent.data?.let { uri ->  imageUris.add(uri) }

        if (imageUris.isNotEmpty()) {
            viewModel.addPropertyImages(imageUris)
            requireActivity().showToast("${imageUris.size} selected successfully")
        }
    }

    private fun onCameraPermissionDenied() {
        logInfo("User denied the camera permission")
        mainActivity.showToast("Please provide camera permission to take pictures")
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

    private fun showAnotherPhotoDialog() {
        AlertDialog.Builder(mainActivity)
            .setMessage("Take another photo ?")
            .setPositiveButton("Yes") {_, _ ->
                imageUploadHelper.checkCameraPermissionAndOpenCamera(mainActivity)
            }
            .setNegativeButton("Done", null)
            .show()
    }

    private fun showAddImageOptions() {
        val options = arrayOf("Camera", "Gallery")
        AlertDialog.Builder(mainActivity)
            .setTitle("Add Image")
            .setItems(options) { _, which ->
                when(which) {
                    0 -> imageUploadHelper.checkCameraPermissionAndOpenCamera(mainActivity)
                    1 -> imageUploadHelper.openImagePicker()
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