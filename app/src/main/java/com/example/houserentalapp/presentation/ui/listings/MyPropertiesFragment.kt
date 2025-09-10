package com.example.houserentalapp.presentation.ui.listings

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R
import com.example.houserentalapp.data.repo.PropertyRepoImpl
import com.example.houserentalapp.databinding.FragmentMyPropertyBinding
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
import kotlin.getValue

/* TODO:
    1. Think about icon click -> edit, un available, delete

 */
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
        currentUser = sharedDataViewModel.currentUser

        setupUI()
        setupViewModel()
        setupListeners()
        setupObservers()

        // Initial Load
        if (myPropertiesViewModel.propertySummariesResult.value !is ResultUI.Success)
            loadProperties()
    }

    fun setupUI() {
        // Always show bottom nav
        mainActivity.showBottomNav()

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

    private fun onPropertyAction(propertyId: Long, action: PropertyLandlordAction) {

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
        val factory = MyPropertiesViewModelFactory(propertyUC, sharedDataViewModel.currentUser)
        myPropertiesViewModel = ViewModelProvider(this, factory)[MyPropertiesViewModel::class]
        filtersViewModel = ViewModelProvider(this)[FiltersViewModel::class]

        // Set Initial Filters
        if (filtersViewModel.filters.value?.onlyUserProperties == false)
            filtersViewModel.setOnlyLandlordProperty(true)
    }

    fun setupListeners() {
        binding.fabAddProperty.setOnClickListener {
            mainActivity.loadFragment(CreatePropertyFragment(), true)
        }
    }

    fun setupObservers() {
        myPropertiesViewModel.propertySummariesResult.observe(viewLifecycleOwner) { result ->
            when(result) {
                is ResultUI.Success<List<PropertySummaryUI>> -> {
                    logInfo("success")
                    myPropertiesAdapter.setDataList(result.data)
                    hideProgressBar()
                }
                is ResultUI.Error -> {
                    hideProgressBar()
                    logError("error occured")
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