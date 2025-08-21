package com.example.houserentalapp.presentation.ui.profile

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentPropertiesListBinding
import com.example.houserentalapp.presentation.ui.MainActivity


// TODO: Show Navigation when user on top position
class PropertiesListFragment : Fragment(R.layout.fragment_properties_list) {
    private lateinit var binding: FragmentPropertiesListBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
//        (context as MainActivity).showBottomNav()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPropertiesListBinding.bind(view)
    }
}