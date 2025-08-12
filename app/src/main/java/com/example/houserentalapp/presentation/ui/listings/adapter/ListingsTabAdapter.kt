package com.example.houserentalapp.presentation.ui.listings.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.houserentalapp.presentation.ui.listings.LeadsFragment
import com.example.houserentalapp.presentation.ui.listings.MyPropertyFragment

class ListingsTabAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> MyPropertyFragment()
            else -> LeadsFragment()
        }
    }

    override fun getItemCount() = 2
}