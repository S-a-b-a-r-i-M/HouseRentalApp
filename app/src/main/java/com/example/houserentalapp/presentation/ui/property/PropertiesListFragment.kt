package com.example.houserentalapp.presentation.ui.property

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentPropertiesListBinding
import com.example.houserentalapp.domain.model.PropertyFilters
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.model.getAddedFiltersCount
import com.example.houserentalapp.presentation.model.PropertySummaryUI
import com.example.houserentalapp.presentation.ui.BundleKeys
import com.example.houserentalapp.presentation.ui.NavigationDestination
import com.example.houserentalapp.presentation.ui.base.BaseFragment
import com.example.houserentalapp.presentation.ui.interfaces.BottomNavController
import com.example.houserentalapp.presentation.ui.property.adapter.PropertiesAdapter
import com.example.houserentalapp.presentation.ui.property.viewmodel.FiltersViewModel
import com.example.houserentalapp.presentation.ui.property.viewmodel.PropertiesListViewModel
import com.example.houserentalapp.presentation.ui.property.viewmodel.PropertiesListViewModelFactory
import com.example.houserentalapp.presentation.ui.sharedviewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.logWarning
import com.example.houserentalapp.presentation.utils.extensions.onBackPressedNavigateBack
import com.example.houserentalapp.presentation.utils.helpers.getScrollListener
import com.example.houserentalapp.presentation.utils.helpers.setSystemBarBottomPadding

class PropertiesListFragment : BaseFragment(R.layout.fragment_properties_list) {
    private lateinit var binding: FragmentPropertiesListBinding
    private lateinit var bottomNavController: BottomNavController
    private lateinit var currentUser: User
    private lateinit var propertiesAdapter: PropertiesAdapter
    private lateinit var propertiesViewModel: PropertiesListViewModel
    private val filtersViewModel: FiltersViewModel by activityViewModels()
    private val sharedDataViewModel: SharedDataViewModel by activityViewModels()
    private val filterBottomSheet: PropertyFilterBottomSheet by lazy { PropertyFilterBottomSheet() }

    private var hideBottomNav = false
    private var onlyShortlisted = false
    private var hideToolBar = false
    private var searchQuery: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        bottomNavController = context as BottomNavController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            hideBottomNav = it.getBoolean(BundleKeys.HIDE_BOTTOM_NAV, hideBottomNav)
            hideToolBar = it.getBoolean(BundleKeys.HIDE_TOOLBAR, hideToolBar)
            onlyShortlisted = it.getBoolean(BundleKeys.ONLY_SHORTLISTED, onlyShortlisted)
            searchQuery = it.getString("searchQuery")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPropertiesListBinding.bind(view)
        // FROM SHARED VIEW MODEL
        currentUser = sharedDataViewModel.currentUserData

        setupUI()
        setupViewModel()
        setupListeners()
        setupObservers()

        if (onlyShortlisted)
            // Only shortlisted will make this page as standalone
            onBackPressedNavigateBack()

        if (savedInstanceState == null) {
            // Initial Load Data
            if (onlyShortlisted)
                filtersViewModel.setPropertyFilters(PropertyFilters(onlyShortlisted = true))
            else
                sharedDataViewModel.getAndClearFilters()?.let {
                    filtersViewModel.setPropertyFilters(it)
                }

            loadProperties()
        }
    }

    private fun loadProperties() {
        propertiesViewModel.loadPropertySummaries(filtersViewModel.filters.value)
    }

    fun setupUI() {
        if (hideBottomNav) {
            // Add paddingBottom to avoid system bar overlay
            setSystemBarBottomPadding(binding.root)
            bottomNavController.hideBottomNav()
        }
        else
            bottomNavController.showBottomNav()

        with(binding) {
          // RecyclerView
            propertiesAdapter = PropertiesAdapter(
                ::handleOnPropertyClick, ::handleShortlistToggle
            )
            rvProperty.apply {
                layoutManager = LinearLayoutManager(_context)
                adapter = propertiesAdapter
                val scrollListener = getScrollListener(
                    { propertiesViewModel.hasMore },
                    ::loadProperties
                )
                addOnScrollListener(scrollListener)
            }

            toolBarLayout.visibility = if (hideToolBar) View.GONE else View.VISIBLE

            // If text view is not completely visible then scroll that
            searchBar.textView.apply {
                isSingleLine = true
                ellipsize = TextUtils.TruncateAt.MARQUEE
                isSelected = true
                marqueeRepeatLimit = -1
            }
        }
    }

    fun setupViewModel() {
        val factory = PropertiesListViewModelFactory(_context, currentUser)
        propertiesViewModel = ViewModelProvider(this, factory)[PropertiesListViewModel::class.java]
    }

    fun setupListeners() {
        with(binding) {
            backImgBtn.root.setOnClickListener {
                parentFragmentManager.popBackStack()
            }

            searchBar.setOnClickListener {
                val bundle = Bundle().apply {
                    putString(BundleKeys.SEARCH_QUERY, binding.searchBar.text.toString())
                }
                 navigateTo(NavigationDestination.InPlaceSearch(bundle))
            }

            btnFilters.setOnClickListener {
                filterBottomSheet.show(parentFragmentManager, "FilterBottomSheet")
            }
        }
    }

    private fun handleOnPropertyClick(propertyId: Long) {
        val bundle = Bundle().apply {
            putLong(BundleKeys.PROPERTY_ID, propertyId)
            putBoolean(BundleKeys.IS_TENANT_VIEW, true)
            putBoolean(BundleKeys.HIDE_AND_SHOW_BOTTOM_NAV, onlyShortlisted)
        }

        navigateTo(NavigationDestination.SinglePropertyDetail(bundle))
    }

    private fun handleShortlistToggle(propertyId: Long) {
        propertiesViewModel.togglePropertyShortlist(
            propertyId,
            { isShortlisted ->
                val message = if (isShortlisted)
                    "Added to shortlisted"
                else
                    "Removed from shortlisted"
                Toast.makeText(_context, message, Toast.LENGTH_SHORT).show()
            },
            {
                Toast.makeText(_context, "Retry later", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun updateFiltersBadgeCount() {
        val filters = filtersViewModel.filters.value ?: run {
            logWarning("filtersViewModel filters value is null, hence can't count badge")
            return
        }

        val count = filters.getAddedFiltersCount() - 1 // Minus Search Query
        if (count == 0)
            binding.btnFiltersBadge.visibility = View.GONE
        else {
            binding.btnFiltersBadge.visibility = View.VISIBLE
            binding.btnFiltersBadge.text = count.toString()
        }
    }

    private fun shouldShowNoDataPlaceHolder() {
        // Placeholder
        val noDataPlaceHolderView = binding.noDataPlaceHolderView.root
        if (propertiesAdapter.itemCount == 0) {
            noDataPlaceHolderView.visibility = View.VISIBLE
            noDataPlaceHolderView.findViewById<TextView>(R.id.tvNoDataMsg)?.let { textView ->
                textView.text = getString(R.string.no_properties_found)
            }
        }
        else {
            noDataPlaceHolderView.visibility = View.GONE
        }
    }

    private fun setupObservers() {
        propertiesViewModel.propertySummariesResult.observe(viewLifecycleOwner) { result ->
            when(result) {
                is ResultUI.Success<List<PropertySummaryUI>> -> {
                    logInfo("success")
                    hideProgressBar()
                    propertiesAdapter.setDataList(
                        result.data,
                        propertiesViewModel.hasMore
                    )
                    shouldShowNoDataPlaceHolder()
                    updateFiltersBadgeCount()
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

        filtersViewModel.filters.observe(viewLifecycleOwner) { filtersData ->
            if (filtersData.searchQuery.isNotEmpty())
                binding.searchBar.setText(filtersData.searchQuery)
        }

        filtersViewModel.applyFilters.observe(viewLifecycleOwner) { shouldLoad ->
            if (shouldLoad) {
                loadProperties()
                filtersViewModel.onFiltersApplied()
            }
        }

        sharedDataViewModel.updatedPropertyId.observe(viewLifecycleOwner) {
            if (it != null){
                sharedDataViewModel.clearUpdatedPropertyId()
                propertiesViewModel.loadUpdatedPropertySummary(it)
            }
        }
    }

    fun showProgressBar() {
        binding.noDataPlaceHolderView.root.visibility = View.GONE // While Loading remove place holder
        propertiesAdapter.isLoading = true
    }

    fun hideProgressBar() {
        propertiesAdapter.isLoading = false
    }
}