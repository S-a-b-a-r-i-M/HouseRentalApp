package com.example.houserentalapp.presentation.ui.property

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentPropertyDetailTenantViewBinding
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.presentation.ui.FragmentArgKey
import com.example.houserentalapp.presentation.ui.base.BaseFragment
import com.example.houserentalapp.presentation.ui.interfaces.BottomNavController
import com.example.houserentalapp.presentation.ui.sharedviewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.utils.extensions.loadChildFragment
import com.example.houserentalapp.presentation.utils.extensions.loadFragment
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.extensions.logError
import kotlin.getValue

class PropertyDetailTenantViewFragment
    : BaseFragment(R.layout.fragment_property_detail_tenant_view) {

    private lateinit var binding: FragmentPropertyDetailTenantViewBinding
    private lateinit var bottomNavController: BottomNavController
    private lateinit var currentUser: User

    private val sharedDataViewModel: SharedDataViewModel by activityViewModels()
    private val _context: Context get() = requireContext()

    private var propertyId: Long = -1L
    private var hideAndShowBottomNav = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        bottomNavController = context as BottomNavController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        propertyId = arguments?.getLong(FragmentArgKey.PROPERTY_ID) ?: run {
            logError("Selected property id is not found in bundle")
            parentFragmentManager.popBackStack()
            return
        }
        hideAndShowBottomNav = arguments?.getBoolean(FragmentArgKey.HIDE_AND_SHOW_BOTTOM_NAV) ?: false

        logDebug("Received arguments \n" +
                "PROPERTY_ID_KEY: $propertyId" +
                "HIDE_AND_SHOW_BOTTOM_NAV_KEY: $hideAndShowBottomNav"
        )

        // Take Current User
        currentUser = sharedDataViewModel.currentUserData
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPropertyDetailTenantViewBinding.bind(view)

        setupUI()

        // ON FRAGMENT FIRST CREATION
        if (savedInstanceState == null) {
            loadSinglePropertyDetailFragment()
        }

        @SuppressLint("ClickableViewAccessibility")
        binding.fragmentContainerProperty.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            true
        }
    }

    private fun loadSinglePropertyDetailFragment() {
        val destination = SinglePropertyDetailFragment()
        destination.arguments =  Bundle().apply {
            putLong(FragmentArgKey.PROPERTY_ID, propertyId)
            putBoolean(FragmentArgKey.IS_TENANT_VIEW, true)
            putBoolean(FragmentArgKey.HIDE_AND_SHOW_BOTTOM_NAV, false)
        }

        loadChildFragment(
            destination, binding.fragmentContainerProperty.id
        )

         // (_context as AppCompatActivity).loadFragment(destination, binding.fragmentContainerProperty.id)
    }

    private fun setupUI() {
        // Always hide bottom nav
        bottomNavController.hideBottomNav()

        with(binding) {

        }
    }

    override fun onDetach() {
        super.onDetach()
        if (hideAndShowBottomNav)
            bottomNavController.showBottomNav()
    }
}