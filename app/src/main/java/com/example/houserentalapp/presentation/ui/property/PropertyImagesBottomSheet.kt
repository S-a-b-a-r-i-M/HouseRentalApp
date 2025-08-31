package com.example.houserentalapp.presentation.ui.property

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentPropertyImagesBottomSheetBinding
import com.example.houserentalapp.presentation.ui.property.adapter.PropertyImagesEditAdapter
import com.example.houserentalapp.presentation.ui.property.viewmodel.CreatePropertyViewModel
import com.example.houserentalapp.presentation.utils.extensions.createPropertyViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PropertyImagesBottomSheet : BottomSheetDialogFragment(R.layout.fragment_property_images_bottom_sheet) {
    private lateinit var binding: FragmentPropertyImagesBottomSheetBinding
    private lateinit var imagesAdapter: PropertyImagesEditAdapter
    private val viewModel: CreatePropertyViewModel by activityViewModels {
        createPropertyViewModelFactory()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPropertyImagesBottomSheetBinding.bind(view)

        setupRecyclerView()
        observeViewModel()
        setClickListeners()
    }


    fun setupRecyclerView() {
        imagesAdapter = PropertyImagesEditAdapter { uri: Uri ->
            viewModel.removePropertyImage(uri)
        }
        binding.rvImages.apply {
            adapter = imagesAdapter
            layoutManager = GridLayoutManager(context, 2)
        }
    }

    private fun observeViewModel() {
        viewModel.imageUris.observe(viewLifecycleOwner) { uris ->
            imagesAdapter.setData(uris)
        }
    }

    private fun setClickListeners() {
        with(binding) {
            btnClose.setOnClickListener {
                dismiss()
            }
        }
    }
}