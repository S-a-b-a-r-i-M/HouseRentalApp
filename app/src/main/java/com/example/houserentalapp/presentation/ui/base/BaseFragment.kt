package com.example.houserentalapp.presentation.ui.base

import android.content.Context
import androidx.fragment.app.Fragment
import com.example.houserentalapp.presentation.ui.NavigationDestination
import com.example.houserentalapp.presentation.ui.interfaces.FragmentNavigationHandler


abstract class BaseFragment(layoutId: Int) : Fragment(layoutId) {
    lateinit var navigationHandler: FragmentNavigationHandler
    protected val _context: Context get() = requireContext()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationHandler = context as FragmentNavigationHandler
    }

    fun navigateTo(destination: NavigationDestination) {
        navigationHandler.navigateTo(destination)
    }
}