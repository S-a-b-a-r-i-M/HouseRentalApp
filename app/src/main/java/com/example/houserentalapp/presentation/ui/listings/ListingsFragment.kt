package com.example.houserentalapp.presentation.ui.listings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentListingsBinding

class ListingsFragment : Fragment() {
    private lateinit var binding: FragmentListingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_listings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentListingsBinding.bind(view)
    }
}