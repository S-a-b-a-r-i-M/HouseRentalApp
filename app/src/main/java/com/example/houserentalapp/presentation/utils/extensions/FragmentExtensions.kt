package com.example.houserentalapp.presentation.utils.extensions

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.houserentalapp.presentation.ui.base.BaseFragment

fun BaseFragment.onBackPressedNavigateBack() {
    requireActivity().onBackPressedDispatcher.addCallback(
        viewLifecycleOwner,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigationHandler.navigateBack()
            }
        }
    )
}

fun Fragment.loadChildFragment(fragment: Fragment, containerId: Int) {
    childFragmentManager.beginTransaction()
        .replace(containerId, fragment)
        .commit()
}