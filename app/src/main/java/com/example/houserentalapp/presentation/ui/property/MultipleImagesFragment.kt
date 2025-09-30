package com.example.houserentalapp.presentation.ui.property

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentMultipleImagesBinding
import com.example.houserentalapp.domain.model.ImageSource
import com.example.houserentalapp.presentation.ui.property.adapter.ImagePagerAdapter
import com.example.houserentalapp.presentation.ui.sharedviewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.utils.helpers.loadImageSourceToImageView
import com.google.android.material.tabs.TabLayoutMediator

class MultipleImagesFragment : Fragment(R.layout.fragment_multiple_images) {
    private lateinit var binding: FragmentMultipleImagesBinding
    private var imagesResources: List<ImageSource> = emptyList()
    private val sharedDataViewModel : SharedDataViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMultipleImagesBinding.bind(view)
        imagesResources = sharedDataViewModel.imageSources

        setupUI()
        setListener()
    }

    private fun setupUI() {
        val adapter = ImagePagerAdapter(imagesResources)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            // Create custom tab view with small image
            val tabView = LayoutInflater.from(requireContext())
                .inflate(R.layout.custom_tab_image, null)

            val tabImage = tabView.findViewById<ImageView>(R.id.tab_image)
            loadImageSourceToImageView(imagesResources[position], tabImage)

            tab.customView = tabView
        }.attach()
    }

    private fun setListener() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}