package com.example.houserentalapp.presentation.ui.home

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentFiltersBinding
import com.example.houserentalapp.presentation.ui.MainActivity
import com.example.houserentalapp.presentation.ui.property.PropertiesListFragment
import com.example.houserentalapp.presentation.utils.helpers.setSystemBarBottomPadding

class FiltersFragment : Fragment(R.layout.fragment_filters) {
    private lateinit var binding: FragmentFiltersBinding
    private lateinit var mainActivity: MainActivity

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFiltersBinding.bind(view)

        // Hide Bottom nav.
        mainActivity = context as MainActivity
        mainActivity.hideBottomNav()

        // Add paddingBottom to avoid system bar overlay
        setSystemBarBottomPadding(binding.root)

        setListeners()
    }

    fun setListeners() {
        with(binding) {
            btnSubmit.setOnClickListener {
                mainActivity.loadFragment(
                    PropertiesListFragment(),
                    true
                )
            }
        }
    }

    override fun onDetach() {
        mainActivity.showBottomNav()
        super.onDetach()
    }
}