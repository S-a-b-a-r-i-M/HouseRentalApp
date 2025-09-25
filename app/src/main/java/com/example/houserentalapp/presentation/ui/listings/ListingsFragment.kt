package com.example.houserentalapp.presentation.ui.listings

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentListingsBinding
import com.example.houserentalapp.presentation.ui.listings.viewmodel.LeadsViewModel
import com.example.houserentalapp.presentation.ui.property.viewmodel.FiltersViewModel
import com.example.houserentalapp.presentation.utils.extensions.loadFragment
import com.example.houserentalapp.presentation.utils.extensions.logDebug

class ListingsFragment : Fragment(R.layout.fragment_listings) {
    private lateinit var binding: FragmentListingsBinding
    // Child Fragments View Models
    private lateinit var filtersViewModel: FiltersViewModel
    private lateinit var leadsViewModel: LeadsViewModel

    private val _context: Context get() = requireContext()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentListingsBinding.bind(view)

        setOnClickListeners()
        setupViewModel()
        // ON FRAGMENT FIRST CREATION
        logDebug("savedInstanceState ------> $savedInstanceState")
        if (savedInstanceState == null)
            binding.myPropertiesBtn.performClick() // PERFORM CLICK
    }

    private fun setOnClickListeners() {
        with(binding) {
            myPropertiesBtn.setOnClickListener {
                (_context as AppCompatActivity).loadFragment(
                    MyPropertyFragment(),
                    containerId = listingsFragmentContainer.id,
                )
            }

            myPropertiesBtn.addOnCheckedChangeListener { _, isChecked ->
                myPropertiesBtn.isClickable = !isChecked
            }

            leadsBtn.setOnClickListener {
                (_context as AppCompatActivity).loadFragment(
                    LeadsFragment(),
                    containerId = listingsFragmentContainer.id,
                )
            }

            leadsBtn.addOnCheckedChangeListener { _, isChecked ->
                leadsBtn.isClickable = !isChecked
            }
        }
    }

    private fun setupViewModel() {

    }
}