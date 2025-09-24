package com.example.houserentalapp.presentation.ui.listings

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentListingsBinding
import com.example.houserentalapp.presentation.ui.MainActivity
import com.example.houserentalapp.presentation.utils.extensions.logDebug

class ListingsFragment : Fragment(R.layout.fragment_listings) {
    private lateinit var binding: FragmentListingsBinding
    private lateinit var mainActivity: MainActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentListingsBinding.bind(view)

        setOnClickListeners()

        // ON FRAGMENT FIRST CREATION
        logDebug("savedInstanceState ------> $savedInstanceState")
        if (savedInstanceState == null)
            binding.myPropertiesBtn.performClick() // PERFORM CLICK
    }

    private fun setOnClickListeners() {
        with(binding) {
            myPropertiesBtn.setOnClickListener {
                mainActivity.loadFragment(
                    MyPropertyFragment(),
                    containerId = listingsFragmentContainer.id,
                )
            }

            myPropertiesBtn.addOnCheckedChangeListener { _, isChecked ->
                myPropertiesBtn.isClickable = !isChecked
            }

            leadsBtn.setOnClickListener {
                mainActivity.loadFragment(
                    LeadsFragment(),
                    containerId = listingsFragmentContainer.id,
                )
            }

            leadsBtn.addOnCheckedChangeListener { _, isChecked ->
                leadsBtn.isClickable = !isChecked
            }
        }
    }
}