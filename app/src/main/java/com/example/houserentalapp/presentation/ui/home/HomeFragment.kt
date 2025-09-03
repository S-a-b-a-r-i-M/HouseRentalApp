package com.example.houserentalapp.presentation.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentHomeBinding
import com.example.houserentalapp.presentation.ui.MainActivity
import com.example.houserentalapp.presentation.ui.property.CreatePropertyFragment
import com.example.houserentalapp.presentation.ui.property.PropertiesListFragment
import com.example.houserentalapp.presentation.ui.property.viewmodel.SharedDataViewModel

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val sharedDataViewModel: SharedDataViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        setListeners()
    }

    fun setListeners() {
        val mainActivity = context as MainActivity
        with(binding) {
            searchBar.setOnClickListener {
                sharedDataViewModel.fPropertiesListMap.apply {
                    put(PropertiesListFragment.HIDE_BOTTOM_NAV_KEY, true)
                    put(PropertiesListFragment.HIDE_TOOLBAR_KEY, false)
                    put(PropertiesListFragment.HIDE_FAB_BUTTON_KEY, false)
                    put(PropertiesListFragment.ONLY_SHORTLISTED_KEY, false)
                }

                mainActivity.loadFragment(PropertiesListFragment(), true)
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