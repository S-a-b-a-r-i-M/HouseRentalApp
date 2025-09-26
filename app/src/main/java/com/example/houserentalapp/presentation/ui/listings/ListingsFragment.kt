package com.example.houserentalapp.presentation.ui.listings

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.houserentalapp.R
import com.example.houserentalapp.data.repo.PropertyRepoImpl
import com.example.houserentalapp.databinding.FragmentListingsBinding
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.usecase.PropertyUseCase
import com.example.houserentalapp.presentation.ui.listings.viewmodel.LeadsViewModel
import com.example.houserentalapp.presentation.ui.listings.viewmodel.LeadsViewModelFactory
import com.example.houserentalapp.presentation.ui.listings.viewmodel.MyPropertiesViewModel
import com.example.houserentalapp.presentation.ui.listings.viewmodel.MyPropertiesViewModelFactory
import com.example.houserentalapp.presentation.ui.property.viewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.utils.extensions.logDebug

class ListingsFragment : Fragment(R.layout.fragment_listings) {
    private lateinit var binding: FragmentListingsBinding
    private lateinit var currentUser: User
    // View Models
    private lateinit var myPropertiesViewModel: MyPropertiesViewModel // Child's Usage
    private lateinit var leadsViewModel: LeadsViewModel // Child's Usage
    private val sharedDataViewModel: SharedDataViewModel by activityViewModels()

    private val _context: Context get() = requireContext()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentListingsBinding.bind(view)
        // Take Current User
        currentUser = sharedDataViewModel.currentUserData

        setupViewModel()
        setOnClickListeners()
        // ON FRAGMENT FIRST CREATION
        logDebug("savedInstanceState ------> $savedInstanceState")
        if (savedInstanceState == null)
            binding.myPropertiesBtn.performClick() // PERFORM CLICK
    }

    private fun setupViewModel() {
        val propertyUC = PropertyUseCase(PropertyRepoImpl(requireActivity()))
        val factory1 = MyPropertiesViewModelFactory(propertyUC, currentUser)
        myPropertiesViewModel = ViewModelProvider(this, factory1)[MyPropertiesViewModel::class]

        val factory2 = LeadsViewModelFactory(_context.applicationContext, currentUser)
        leadsViewModel = ViewModelProvider(this, factory2)[LeadsViewModel::class]
    }

    private fun setOnClickListeners() {
        with(binding) {
            myPropertiesBtn.setOnClickListener {
                childFragmentManager.beginTransaction()
                .replace(listingsFragmentContainer.id, MyPropertyFragment())
                .commit()
            }

            myPropertiesBtn.addOnCheckedChangeListener { _, isChecked ->
                myPropertiesBtn.isClickable = !isChecked
            }

            leadsBtn.setOnClickListener {
                childFragmentManager.beginTransaction()
                .replace(listingsFragmentContainer.id, LeadsFragment())
                .commit()
            }

            leadsBtn.addOnCheckedChangeListener { _, isChecked ->
                leadsBtn.isClickable = !isChecked
            }
        }
    }
}