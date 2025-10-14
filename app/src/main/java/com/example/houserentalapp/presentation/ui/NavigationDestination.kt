package com.example.houserentalapp.presentation.ui

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.houserentalapp.presentation.ui.common.SearchViewFragment
import com.example.houserentalapp.presentation.ui.listings.ListingsFragment
import com.example.houserentalapp.presentation.ui.profile.ProfileEditFragment
import com.example.houserentalapp.presentation.ui.property.CreatePropertyActivity
import com.example.houserentalapp.presentation.ui.property.CreatePropertyFragment
import com.example.houserentalapp.presentation.ui.property.MultipleImagesFragment
import com.example.houserentalapp.presentation.ui.property.PropertiesListFragment
import com.example.houserentalapp.presentation.ui.property.SinglePropertyDetailFragment

// ONLY CLASS KNOWS ABOUT THE FRAGMENTS
sealed class NavigationDestination(
    val fragmentClass: Class<out Fragment>? = null,
    val activityClass: Class<out Activity>? = null,
    val args: Bundle? = null,
    val resultCallBack: ((Boolean) -> Unit)? = null,
    val pushToBackStack: Boolean = false,
    val removeExistingHistory: Boolean = false,
) {
    data class SeparateSearch(val bundle: Bundle? = null) : NavigationDestination(
        fragmentClass = SearchViewFragment::class.java,
        args = (bundle ?: Bundle()).apply { putBoolean(BundleKeys.IS_NEW_SEARCH, true) },
        pushToBackStack = true
    )

    data class InPlaceSearch(val bundle: Bundle? = null) : NavigationDestination(
        fragmentClass =SearchViewFragment::class.java,
        args = bundle,
        pushToBackStack = true
    )

    data class PropertyList(val bundle: Bundle? = null)
        : NavigationDestination(
        fragmentClass = PropertiesListFragment::class.java,
        args = bundle,
        pushToBackStack = true,
        removeExistingHistory = true
    )

    data class CreateProperty(
        val bundle: Bundle? = null, val onResult: ((Boolean) -> Unit)? = null
    ) : NavigationDestination(
        activityClass = CreatePropertyActivity::class.java,
        args = (bundle ?: Bundle()).apply {
            putBoolean(BundleKeys.HIDE_AND_SHOW_BOTTOM_NAV, true)
        },
        resultCallBack = onResult,
        pushToBackStack = true
    )

    data class EditProperty(val bundle: Bundle? = null) : NavigationDestination(
        fragmentClass = CreatePropertyFragment::class.java,
        args = bundle,
        pushToBackStack = true
    )

    data class SinglePropertyDetail(val bundle: Bundle? = null) : NavigationDestination(
        fragmentClass = SinglePropertyDetailFragment::class.java,
        args = bundle,
        pushToBackStack = true
    )

    data class RecommendedSinglePropertyDetail(val bundle: Bundle? = null) : NavigationDestination(
        fragmentClass = SinglePropertyDetailFragment::class.java,
        args = bundle,
        pushToBackStack = true
    )

    data class MultipleImages(val bundle: Bundle? = null) : NavigationDestination(
        fragmentClass = MultipleImagesFragment::class.java,
        args = bundle,
        pushToBackStack = true
    )

    data class ShortlistedProperties(val bundle: Bundle? = null) : NavigationDestination(
        fragmentClass = PropertiesListFragment::class.java,
        args = bundle,
        pushToBackStack = true
    )

    data class ProfileEdit(val bundle: Bundle? = null) : NavigationDestination(
        fragmentClass = ProfileEditFragment::class.java,
        args = bundle,
        pushToBackStack = true
    )

    data class MyLeads(val bundle: Bundle? = null) : NavigationDestination(
        fragmentClass = ListingsFragment::class.java,
        args = (bundle ?: Bundle()).apply {
            putString(
                BundleKeys.CHILD_FRAGMENT_NAME,
                ListingsFragment.ChildFragmentName.LEADS.name
            )
        },
        pushToBackStack = true
    )

    data class MyProperties(val bundle: Bundle? = null) : NavigationDestination(
        fragmentClass = ListingsFragment::class.java,
        args = (bundle ?: Bundle()).apply {
            putString(
                BundleKeys.CHILD_FRAGMENT_NAME,
                ListingsFragment.ChildFragmentName.MY_PROPERTIES.name
            )
        },
        pushToBackStack = true
    )
}