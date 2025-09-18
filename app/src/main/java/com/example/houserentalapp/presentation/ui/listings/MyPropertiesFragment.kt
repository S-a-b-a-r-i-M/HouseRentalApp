package com.example.houserentalapp.presentation.ui.listings

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R
import com.example.houserentalapp.data.repo.PropertyRepoImpl
import com.example.houserentalapp.databinding.FragmentMyPropertyBinding
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.usecase.PropertyUseCase
import com.example.houserentalapp.presentation.enums.PropertyLandlordAction
import com.example.houserentalapp.presentation.model.PropertySummaryUI
import com.example.houserentalapp.presentation.ui.MainActivity
import com.example.houserentalapp.presentation.ui.listings.adapter.MyPropertiesAdapter
import com.example.houserentalapp.presentation.ui.listings.viewmodel.MyPropertiesViewModelFactory
import com.example.houserentalapp.presentation.ui.listings.viewmodel.MyPropertiesViewModel
import com.example.houserentalapp.presentation.ui.property.CreatePropertyFragment
import com.example.houserentalapp.presentation.ui.property.SinglePropertyDetailFragment
import com.example.houserentalapp.presentation.ui.property.viewmodel.FiltersViewModel
import com.example.houserentalapp.presentation.ui.property.viewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.logWarning
import com.example.houserentalapp.presentation.utils.extensions.showToast
import kotlin.getValue

class MyPropertyFragment : Fragment(R.layout.fragment_my_property) {
    private lateinit var binding: FragmentMyPropertyBinding
    private lateinit var mainActivity: MainActivity
    private lateinit var currentUser: User
    private lateinit var myPropertiesAdapter: MyPropertiesAdapter
    private lateinit var myPropertiesViewModel: MyPropertiesViewModel
    private lateinit var filtersViewModel: FiltersViewModel
    private val sharedDataViewModel: SharedDataViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMyPropertyBinding.bind(view)
        // Take Current User
        currentUser = sharedDataViewModel.currentUserLD.value ?: run {
            mainActivity.showToast("Login again...")
            mainActivity.finish()
            return
        }

        setupUI()
        setupViewModel()
        setupListeners()
        setupObservers()

        // Initial Load
        if (savedInstanceState == null)
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
                layoutManager = LinearLayoutManager(mainActivity)
                adapter = myPropertiesAdapter
                addOnScrollListener(scrollListener)
            }
        }
    }

    private fun handleOnPropertyClick(propertyId: Long) {
        val destinationFragment = SinglePropertyDetailFragment()
        destinationFragment.arguments = Bundle().apply {
            putLong(SinglePropertyDetailFragment.PROPERTY_ID_KEY, propertyId)
            putBoolean(SinglePropertyDetailFragment.IS_TENANT_VIEW_KEY, false)
            putBoolean(SinglePropertyDetailFragment.HIDE_AND_SHOW_BOTTOM_NAV_KEY, true)
        }

        mainActivity.addFragment(destinationFragment, true)
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
                Toast.makeText(mainActivity, message, Toast.LENGTH_SHORT).show()
            },
            {
                Toast.makeText(mainActivity, "Retry later", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun deleteProperty(propertyId: Long) {
        myPropertiesViewModel.deleteProperty(
            propertyId,
            { mainActivity.showToast("Property deleted successfully") },
            { mainActivity.showToast("Retry later") }
        )
    }

    private fun onPropertyAction(summary: PropertySummary, action: PropertyLandlordAction) {
        when(action) {
            PropertyLandlordAction.EDIT -> {
                val destinationFragment = CreatePropertyFragment()
                destinationFragment.arguments = Bundle().apply {
                    putLong(CreatePropertyFragment.PROPERTY_ID_KEY, summary.id)
                    putBoolean(CreatePropertyFragment.HIDE_AND_SHOW_BOTTOM_NAV, true)
                }
                mainActivity.addFragment(destinationFragment, true)
            }
            PropertyLandlordAction.CHANGE_AVAILABILITY -> {
                val newActiveState = !summary.isActive
                if (newActiveState)
                    updateAvailability(summary.id, true)
                else // To make it unavailable Show confirmation dialog first
                    AlertDialog.Builder(mainActivity)
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
                AlertDialog.Builder(mainActivity)
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
        myPropertiesViewModel.loadPropertySummaries(filtersViewModel.filters.value)
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            val layoutManger = recyclerView.layoutManager as LinearLayoutManager

            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                return // No need to fetch new items while scrolling
            }
            else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (!myPropertiesViewModel.hasMore) return

                val lastVisibleItemPosition =
                    layoutManger.findLastCompletelyVisibleItemPosition() // index
                val totalItemCount = recyclerView.adapter?.itemCount ?: run {
                    logWarning("totalItemCount is not accessible")
                    return
                }
                val shouldLoadMore = (lastVisibleItemPosition + 1) >= totalItemCount
                if (shouldLoadMore) {
                    logInfo("<----------- from onScroll State changed ---------->")
                    loadProperties()
                }
            }
        }
    }

    fun setupViewModel() {
        val propertyUC = PropertyUseCase(PropertyRepoImpl(requireActivity()))
        val factory = MyPropertiesViewModelFactory(propertyUC, currentUser)
        myPropertiesViewModel = ViewModelProvider(this, factory)[MyPropertiesViewModel::class]
        filtersViewModel = ViewModelProvider(this)[FiltersViewModel::class]

        // Set Initial Filters
        if (filtersViewModel.filters.value?.onlyUserProperties == false) {
            filtersViewModel.setOnlyLandlordProperty(true)
            filtersViewModel.setOnlyAvailable(false)
        }
    }

    fun setupListeners() {
        binding.fabAddProperty.setOnClickListener {
            val destinationFragment = CreatePropertyFragment()
            destinationFragment.arguments = Bundle().apply {
                putBoolean(CreatePropertyFragment.HIDE_AND_SHOW_BOTTOM_NAV, true)
            }
            mainActivity.loadFragment(destinationFragment, true)
        }
    }

    fun onDataObserved(propertySummaryUI : List<PropertySummaryUI>) {
        myPropertiesAdapter.setDataList(propertySummaryUI)
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