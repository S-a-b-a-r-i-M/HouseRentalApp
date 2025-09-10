package com.example.houserentalapp.presentation.ui.property

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R
import com.example.houserentalapp.data.repo.PropertyRepoImpl
import com.example.houserentalapp.data.repo.SearchHistoryRepoImpl
import com.example.houserentalapp.data.repo.UserPropertyRepoImpl
import com.example.houserentalapp.databinding.FragmentPropertiesListBinding
import com.example.houserentalapp.domain.usecase.PropertyUseCase
import com.example.houserentalapp.domain.usecase.SearchHistoryUseCase
import com.example.houserentalapp.domain.usecase.TenantRelatedPropertyUseCase
import com.example.houserentalapp.presentation.model.PropertySummaryUI
import com.example.houserentalapp.presentation.ui.MainActivity
import com.example.houserentalapp.presentation.ui.common.SearchViewFragment
import com.example.houserentalapp.presentation.ui.property.adapter.PropertiesAdapter
import com.example.houserentalapp.presentation.ui.property.viewmodel.FiltersViewModel
import com.example.houserentalapp.presentation.ui.property.viewmodel.PropertiesListViewModel
import com.example.houserentalapp.presentation.ui.property.viewmodel.PropertiesListViewModelFactory
import com.example.houserentalapp.presentation.ui.property.viewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.logWarning
import com.example.houserentalapp.presentation.utils.helpers.setSystemBarBottomPadding

/* TODO
    1. FIX: Bottom nav while moving to shortlists page from here
    2. FIX: Sort
 */
class PropertiesListFragment : Fragment(R.layout.fragment_properties_list) {
    private lateinit var binding: FragmentPropertiesListBinding
    private lateinit var mainActivity: MainActivity
    private lateinit var propertiesAdapter: PropertiesAdapter
    private lateinit var propertiesViewModel: PropertiesListViewModel
    private val sharedDataViewModel: SharedDataViewModel by activityViewModels()
    private val filtersViewModel: FiltersViewModel by activityViewModels()
    private val filterBottomSheet: PropertyFilterBottomSheet by lazy { PropertyFilterBottomSheet() }
    private var isScrolling = false
    private var hideBottomNav = false
    private var onlyShortlisted = false
    private var hideToolBar = false
    private var hideFabButton = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPropertiesListBinding.bind(view)

        // Decisions based on received values
        hideBottomNav = sharedDataViewModel.propertiesListStore[HIDE_BOTTOM_NAV_KEY] as? Boolean ?: false
        hideToolBar = sharedDataViewModel.propertiesListStore[HIDE_TOOLBAR_KEY] as? Boolean ?: false
        hideFabButton = sharedDataViewModel.propertiesListStore[HIDE_FAB_BUTTON_KEY] as? Boolean ?: false
        onlyShortlisted = sharedDataViewModel.propertiesListStore[ONLY_SHORTLISTED_KEY] as? Boolean ?: false

        setupUI()
        setupViewModel()
        setupListeners()
        setupObservers()

        // Initial Load Data
        filtersViewModel.setOnlyShortlisted(onlyShortlisted)
        if (propertiesViewModel.propertySummariesResult.value !is ResultUI.Success)
            loadProperties()
    }

    private fun loadProperties() {
        propertiesViewModel.loadPropertySummaries(filtersViewModel.filters.value)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(HIDE_BOTTOM_NAV_KEY, hideBottomNav)
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            val layoutManger = recyclerView.layoutManager as LinearLayoutManager

            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                isScrolling = true
                return // No need to fetch new items while scrolling
            }
            else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                isScrolling = false
                if (!propertiesViewModel.hasMore) return

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

    fun setupUI() {
        if (hideBottomNav)
            mainActivity.hideBottomNav()
        else
            mainActivity.showBottomNav()

        // Add paddingBottom to avoid system bar overlay
        setSystemBarBottomPadding(binding.root)

        with(binding) {
          // RecyclerView
            propertiesAdapter = PropertiesAdapter(
                ::handleOnPropertyClick, ::handleShortlistToggle
            )
            rvProperty.apply {
                layoutManager = LinearLayoutManager(mainActivity)
                adapter = propertiesAdapter
                addOnScrollListener(scrollListener)
            }

            toolBarLayout.visibility = if (hideToolBar) View.GONE else View.VISIBLE

            fabShortlists.visibility = if (hideFabButton) View.GONE else View.VISIBLE

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
        val propertyUC = PropertyUseCase(PropertyRepoImpl(mainActivity))
        val propertyUserActionUC = TenantRelatedPropertyUseCase(UserPropertyRepoImpl(mainActivity))
        val searchHistoryUC = SearchHistoryUseCase(SearchHistoryRepoImpl(mainActivity))
        val currentUser = mainActivity.getCurrentUser()
        val factory = PropertiesListViewModelFactory(
            propertyUC,
            propertyUserActionUC,
            searchHistoryUC,
            currentUser
        )
        propertiesViewModel = ViewModelProvider(this, factory)
            .get(PropertiesListViewModel::class.java)
    }

    fun setupListeners() {
        with(binding) {
            backImgBtn.root.setOnClickListener {
                parentFragmentManager.popBackStack()
            }

            searchBar.setOnClickListener {
                mainActivity.addFragment(SearchViewFragment(), true)
            }

            btnFilters.setOnClickListener {
                filterBottomSheet.show(parentFragmentManager, "FilterBottomSheet")
            }

            fabShortlists.setOnClickListener {
                with(sharedDataViewModel) {
                    resetPropertiesListStore()
                    addToPropertiesListStore(ONLY_SHORTLISTED_KEY, true)
                    addToPropertiesListStore(HIDE_TOOLBAR_KEY, true)
                    addToPropertiesListStore(HIDE_FAB_BUTTON_KEY, true)
                }

                mainActivity.loadFragment(PropertiesListFragment(), true)
//                mainActivity.selectBottomNavOption(NavigationOptions.SHORTLISTS)
            }
        }
    }

    private fun handleOnPropertyClick(propertyId: Long) {
        val destinationFragment = SinglePropertyDetailFragment()
        destinationFragment.arguments = Bundle().apply {
            putLong(SinglePropertyDetailFragment.PROPERTY_ID_KEY, propertyId)
            putBoolean(SinglePropertyDetailFragment.IS_TENANT_VIEW_KEY, true)
            putBoolean(SinglePropertyDetailFragment.HIDE_AND_SHOW_BOTTOM_NAV_KEY, onlyShortlisted)
        }

        mainActivity.addFragment(destinationFragment, true)
    }

    private fun handleShortlistToggle(propertyId: Long) {
        propertiesViewModel.togglePropertyShortlist(
            propertyId,
            { isShortlisted ->
                val message = if (isShortlisted)
                    "Added to shortlisted"
                else
                    "Removed from shortlisted"
                Toast.makeText(mainActivity, message, Toast.LENGTH_SHORT).show()
            },
            {
                Toast.makeText(mainActivity, "Retry later", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun setupObservers() {
        propertiesViewModel.propertySummariesResult.observe(viewLifecycleOwner) { result ->
            when(result) {
                is ResultUI.Success<List<PropertySummaryUI>> -> {
                    logInfo("success")
                    propertiesAdapter.setDataList(result.data)
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
    }

    fun showProgressBar() {
        isScrolling
        binding.progressBar.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    companion object {
        const val HIDE_BOTTOM_NAV_KEY = "hideBottomNav"
        const val ONLY_SHORTLISTED_KEY = "onlyShortlisted"
        const val HIDE_TOOLBAR_KEY = "hideToolBar"
        const val HIDE_FAB_BUTTON_KEY= "hideFabButton"
    }
}