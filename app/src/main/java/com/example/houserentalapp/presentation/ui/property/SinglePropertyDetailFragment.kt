package com.example.houserentalapp.presentation.ui.property

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentSinglePropertyDetailBinding
import com.example.houserentalapp.presentation.utils.helpers.setSystemBarBottomPadding

class SinglePropertyDetailFragment : Fragment() {
    private lateinit var binding: FragmentSinglePropertyDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_single_property_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSinglePropertyDetailBinding.bind(view)

        // Add paddingBottom to avoid system bar overlay
        setSystemBarBottomPadding(binding.root)
    }
}