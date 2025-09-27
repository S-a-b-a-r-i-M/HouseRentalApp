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
import com.example.houserentalapp.presentation.ui.FragmentArgKey
import com.example.houserentalapp.presentation.ui.base.BaseFragment
import com.example.houserentalapp.presentation.ui.listings.viewmodel.LeadsViewModel
import com.example.houserentalapp.presentation.ui.listings.viewmodel.LeadsViewModelFactory
import com.example.houserentalapp.presentation.ui.listings.viewmodel.MyPropertiesViewModel
import com.example.houserentalapp.presentation.ui.listings.viewmodel.MyPropertiesViewModelFactory
import com.example.houserentalapp.presentation.ui.property.viewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.utils.extensions.onBackPressedNavigateBack

class ListingsFragment : BaseFragment(R.layout.fragment_listings) {
    private lateinit var binding: FragmentListingsBinding
    private lateinit var currentUser: User
    // View Models
    private lateinit var myPropertiesViewModel: MyPropertiesViewModel // Child's Usage
    private lateinit var leadsViewModel: LeadsViewModel // Child's Usage
    private val sharedDataViewModel: SharedDataViewModel by activityViewModels()

    private val _context: Context get() = requireContext()
    private var childFragmentName = ChildFragmentName.MY_PROPERTIES

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentName = ChildFragmentName.valueOf(
            arguments?.getString(FragmentArgKey.CHILD_FRAGMENT_NAME)
                ?: childFragmentName.name
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentListingsBinding.bind(view)
        // Take Current User
        currentUser = sharedDataViewModel.currentUserData

        setupViewModel()
        setOnClickListeners()
        onBackPressedNavigateBack()

        // ON FRAGMENT FIRST CREATION
        if (savedInstanceState == null) {
            if (childFragmentName == ChildFragmentName.MY_PROPERTIES)
                binding.myPropertiesBtn.performClick()
            else
                binding.leadsBtn.performClick()
        }
    }

    private fun setupViewModel() {
        val propertyUC = PropertyUseCase(PropertyRepoImpl(requireActivity()))
        val factory1 = MyPropertiesViewModelFactory(propertyUC, currentUser)
        myPropertiesViewModel = ViewModelProvider(this, factory1)[MyPropertiesViewModel::class]

        val factory2 = LeadsViewModelFactory(_context.applicationContext, currentUser)
        leadsViewModel = ViewModelProvider(this, factory2)[LeadsViewModel::class]
    }

    private fun loadChildFragment(
        fragment: Fragment, containerId: Int = binding.listingsFragmentContainer.id
    ) {
        childFragmentManager.beginTransaction()
        .replace(containerId, fragment)
        .commit()
    }

    private fun setOnClickListeners() {
        with(binding) {
            myPropertiesBtn.setOnClickListener { loadChildFragment(MyPropertyFragment()) }

            myPropertiesBtn.addOnCheckedChangeListener { _, isChecked ->
                myPropertiesBtn.isClickable = !isChecked
            }

            leadsBtn.setOnClickListener { loadChildFragment(LeadsFragment()) }

            leadsBtn.addOnCheckedChangeListener { _, isChecked ->
                leadsBtn.isClickable = !isChecked
            }
        }
    }

    enum class ChildFragmentName() {
        MY_PROPERTIES,
        LEADS
    }
}