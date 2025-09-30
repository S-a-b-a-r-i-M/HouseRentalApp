package com.example.houserentalapp.presentation.ui.listings

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentMyPropertyBinding
import com.example.houserentalapp.domain.model.PropertyFilters
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.presentation.enums.PropertyLandlordAction
import com.example.houserentalapp.presentation.model.PropertySummaryUI
import com.example.houserentalapp.presentation.ui.FragmentArgKey
import com.example.houserentalapp.presentation.ui.NavigationDestination
import com.example.houserentalapp.presentation.ui.base.BaseFragment
import com.example.houserentalapp.presentation.ui.interfaces.BottomNavController
import com.example.houserentalapp.presentation.ui.listings.adapter.MyPropertiesAdapter
import com.example.houserentalapp.presentation.ui.listings.viewmodel.MyPropertiesViewModel
import com.example.houserentalapp.presentation.ui.sharedviewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.onBackPressedNavigateBack
import com.example.houserentalapp.presentation.utils.extensions.showToast
import com.example.houserentalapp.presentation.utils.helpers.getScrollListener

class MyPropertyFragment : BaseFragment(R.layout.fragment_my_property) {
    private lateinit var binding: FragmentMyPropertyBinding
    private lateinit var bottomNavController: BottomNavController
    private lateinit var currentUser: User
    private lateinit var myPropertiesAdapter: MyPropertiesAdapter
    private val myPropertiesViewModel: MyPropertiesViewModel by viewModels({ requireParentFragment() })
    private val sharedDataViewModel: SharedDataViewModel by activityViewModels()
    private val _context: Context get() = requireContext()
    private var propertyFilters = PropertyFilters(
        onlyUserProperties = true,
        onlyAvailable = false
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        bottomNavController = context as BottomNavController
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMyPropertyBinding.bind(view)
        // Take Current User
        currentUser = sharedDataViewModel.currentUserData

        setupUI()
        setupListeners()
        setupObservers()
        onBackPressedNavigateBack()

        // Initial Load
        if (myPropertiesViewModel.propertySummariesResult.value !is ResultUI.Success)
            loadProperties()
    }

    fun setupUI() {
        with(binding) {
            // RecyclerView
            myPropertiesAdapter = MyPropertiesAdapter(
                ::handleOnPropertyClick,
                ::onPropertyAction
            )
            rvProperty.apply {
                layoutManager = LinearLayoutManager(_context)
                adapter = myPropertiesAdapter
                val scrollListener = getScrollListener(
                    { myPropertiesViewModel.hasMore },
                    ::loadProperties
                )
                addOnScrollListener(scrollListener)
            }
        }
    }

    private fun handleOnPropertyClick(propertyId: Long) {
        val bundle = Bundle().apply {
            putLong(FragmentArgKey.PROPERTY_ID, propertyId)
            putBoolean(FragmentArgKey.IS_TENANT_VIEW, false)
            putBoolean(FragmentArgKey.HIDE_AND_SHOW_BOTTOM_NAV, true)
        }
        navigateTo(NavigationDestination.SinglePropertyDetails(bundle))
    }

    private fun updateAvailability(propertyId: Long, isActive: Boolean) {
        myPropertiesViewModel.updatePropertyAvailability(
            propertyId,
            isActive,
            { isActivated ->
                val message = if (isActivated)
                    "Property activated successfully"
                else
                    "Property in-activated successfully"
                _context.showToast(message)
            },
            {
                _context.showToast("Retry later")
            }
        )
    }

    private fun deleteProperty(propertyId: Long) {
        myPropertiesViewModel.deleteProperty(
            propertyId,
            { _context.showToast("Property deleted successfully") },
            { _context.showToast("Retry later") }
        )
    }

    private fun onPropertyAction(summary: PropertySummary, action: PropertyLandlordAction) {
        when(action) {
            PropertyLandlordAction.EDIT -> {
                val bundle = Bundle().apply {
                    putLong(FragmentArgKey.PROPERTY_ID, summary.id)
                    putBoolean(FragmentArgKey.HIDE_AND_SHOW_BOTTOM_NAV, true)
                }
                navigateTo(NavigationDestination.EditProperty(bundle))
            }
            PropertyLandlordAction.CHANGE_AVAILABILITY -> {
                val newActiveState = !summary.isActive
                if (newActiveState)
                    updateAvailability(summary.id, true)
                else // To make it unavailable Show confirmation dialog first
                    AlertDialog.Builder(_context)
                        .setTitle("Make Property Unavailable")
                        .setMessage("This property will be hidden from listings. Continue?")
                        .setPositiveButton("Yes") { dialog, which ->
                            updateAvailability(summary.id,false)
                        }
                        .setNegativeButton("No", null)
                        .show()
            }
            PropertyLandlordAction.DELETE -> {
                // Show confirmation dialog first
                AlertDialog.Builder(_context)
                    .setTitle("Delete Property")
                    .setMessage("This action cannot be undone. Delete this property?")
                    .setPositiveButton("Delete") { _, _ ->
                        deleteProperty(summary.id)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun loadProperties() {
        // Can add further more filters if needed
        myPropertiesViewModel.loadPropertySummaries(propertyFilters)
    }

    fun setupListeners() {
        binding.fabAddProperty.setOnClickListener {
            val bundle = Bundle().apply {
                putBoolean(FragmentArgKey.HIDE_AND_SHOW_BOTTOM_NAV, true)
            }
            navigateTo(NavigationDestination.CreateProperty(bundle))
        }
    }

    fun onDataObserved(propertySummaryUI : List<PropertySummaryUI>) {
        myPropertiesAdapter.setPropertySummaryUiList(
            propertySummaryUI, myPropertiesViewModel.hasMore
        )
        // PlaceHolder
        val noDataPlaceHolderView = binding.noDataPlaceHolderView.root
        if (myPropertiesAdapter.itemCount == 0) {
            noDataPlaceHolderView.visibility = View.VISIBLE
            noDataPlaceHolderView.findViewById<TextView>(R.id.tvNoDataMsg)?.let { textView ->
                textView.text = getString(R.string.post_your_properties_to_see_the_magic)
            }
        }
        else {
            noDataPlaceHolderView.visibility = View.GONE
        }
    }

    fun setupObservers() {
        myPropertiesViewModel.propertySummariesResult.observe(viewLifecycleOwner) { result ->
            when(result) {
                is ResultUI.Success<List<PropertySummaryUI>> -> {
                    logInfo("success")
                    onDataObserved(result.data)
                    hideProgressBar()
                }
                is ResultUI.Error -> {
                    hideProgressBar()
                    logError("error occurred")
                }
                ResultUI.Loading -> {
                    showProgressBar()
                    logInfo("loading")
                }
            }
        }

        sharedDataViewModel.updatedPropertyId.observe(viewLifecycleOwner) {
            if (it != null) {
                sharedDataViewModel.clearUpdatedPropertyId()
                myPropertiesViewModel.loadUpdatedPropertySummary(it)
            }
        }
    }

    fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    override fun onResume() {
        // While adding fragment none of the methods are getting invoked
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }
}