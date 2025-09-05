package com.example.houserentalapp.presentation.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentHomeBinding
import com.example.houserentalapp.presentation.ui.common.SearchViewFragment
import com.example.houserentalapp.presentation.ui.MainActivity
import com.example.houserentalapp.presentation.ui.property.CreatePropertyFragment
import com.example.houserentalapp.presentation.ui.property.viewmodel.SharedDataViewModel

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding
    private val sharedDataViewModel: SharedDataViewModel by activityViewModels()
    private lateinit var mainActivity: MainActivity

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        mainActivity = context as MainActivity

        setupUI()
        setListeners()
    }

    private fun setupUI() {
        // Always show bottom nav on HomeFragment
        mainActivity.showBottomNav()

        with(binding) {

        }
    }

    fun setListeners() {
        with(binding) {
            searchBar.setOnClickListener {
                // Let the search view know this is a fresh search
                val destinationFragment = SearchViewFragment()
                destinationFragment.arguments = Bundle().apply {
                    putBoolean(SearchViewFragment.IS_NEW_SEARCH, true)
                }

                mainActivity.loadFragment(destinationFragment, true)
            }

            btnPostProperty.setOnClickListener {
                mainActivity.loadFragment(
                    CreatePropertyFragment(),
                    true
                )
            }
        }
    }
}