package com.example.houserentalapp.presentation.ui.common

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentFiltersBinding
import com.example.houserentalapp.domain.model.PropertyFilters
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.presentation.ui.NavigationDestination
import com.example.houserentalapp.presentation.ui.base.BaseFragment
import com.example.houserentalapp.presentation.ui.common.adapter.SearchHistoryAdapter
import com.example.houserentalapp.presentation.ui.common.viewmodel.SearchHistoryViewModel
import com.example.houserentalapp.presentation.ui.common.viewmodel.SearchHistoryViewModelFactory
import com.example.houserentalapp.presentation.ui.interfaces.BottomNavController
import com.example.houserentalapp.presentation.ui.property.PropertiesListFragment
import com.example.houserentalapp.presentation.ui.property.viewmodel.FiltersViewModel
import com.example.houserentalapp.presentation.ui.property.viewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.helpers.setSystemBarBottomPadding

class SearchViewFragment : BaseFragment(R.layout.fragment_filters) {
    private lateinit var binding: FragmentFiltersBinding
    private lateinit var bottomNavController: BottomNavController
    private lateinit var searchHistoryAdapter: SearchHistoryAdapter
    private lateinit var searchHistoryViewModel : SearchHistoryViewModel
    private lateinit var currentUser: User
    private val sharedDataViewModel: SharedDataViewModel by activityViewModels()
    private val filtersViewModel: FiltersViewModel by activityViewModels()
    private val _context: Context get() = requireContext()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        bottomNavController = context as BottomNavController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Take Current User
        currentUser = sharedDataViewModel.currentUserData

        val isNewSearch = arguments?.getBoolean(IS_NEW_SEARCH) ?: false
        if (isNewSearch) {
            // Reset Filters on the first creation of this fragment(hence used arguments)
            logDebug("isNewSearch given as true so all the previous filters are cleared")
            filtersViewModel.resetFilters()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFiltersBinding.bind(view)

        setupUI()
        setupViewModel()
        setupListeners()
        setupObservers()

        // Load Search Histories
        if (savedInstanceState == null)
            searchHistoryViewModel.loadSearchHistories(currentUser.id)
    }

    private fun setupUI() {
        // Always hide bottom nav on this Fragment
        bottomNavController.hideBottomNav()

        // Add paddingBottom to avoid system bar overlay
        setSystemBarBottomPadding(binding.root)

        with(binding) {
            // Recycler View
            searchHistoryAdapter = SearchHistoryAdapter(::onHistoryClick)
            rvSearchHistory.apply {
                adapter = searchHistoryAdapter
                layoutManager = LinearLayoutManager(_context)
            }

            // Search View
            updateSubmitButtonState(etSearch.text.toString().trim().length > 2)
        }
    }

    private fun updateSubmitButtonState(enable: Boolean) {
        val button = binding.btnSubmit
        if (enable == button.isEnabled) return

        if (enable)
            button.apply {
                isEnabled = true
                alpha = 1f
            }
        else
            button.apply {
                isEnabled = false
                alpha = 0.7f
            }
    }

    private fun setupViewModel() {
        val factory = SearchHistoryViewModelFactory(_context.applicationContext)
        searchHistoryViewModel=ViewModelProvider(this, factory)[SearchHistoryViewModel::class]
    }

    private fun onHistoryClick(filters: PropertyFilters) {
        filtersViewModel.setPropertyFilters(filters)
        navigateToPropertiesListFragment()
    }

    private fun navigateToPropertiesListFragment() {
        val bundle = Bundle().apply {
            putBoolean(PropertiesListFragment.HIDE_BOTTOM_NAV_KEY, true)
        }
        navigateTo(NavigationDestination.PropertyList(bundle))
    }

    private fun applySearchQuery() {
        val searchQuery = binding.etSearch.text.toString()
        if (searchQuery.isNotEmpty())
            filtersViewModel.setSearchQuery(searchQuery)
    }

    private fun setupListeners() {
        with(binding) {
            // Setup SearchView text changes
            etSearch.doOnTextChanged { text, start, before, count ->
                val query = text?.toString()?.trim() ?: ""
                updateSubmitButtonState(query.length > 2)
                if (query.length < 3)
                    tvHelperMsg.apply {
                        visibility = View.VISIBLE
                        this.text = context.getString(R.string.minimum_3_characteres_required_to_search)
                    }
                else
                    tvHelperMsg.visibility = View.GONE
            }

            // Listens Search Icon Click in Keyboard
            etSearch.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (btnSubmit.isEnabled) {
                        applySearchQuery()
                        navigateToPropertiesListFragment()
                    }
                    true
                }
                else
                    false
            }

            backImgBtn.setOnClickListener { parentFragmentManager.popBackStack() }

            btnSubmit.setOnClickListener {
                applySearchQuery()
                navigateToPropertiesListFragment()
            }
        }
    }

    private fun setupObservers() {
        // Save Changes Into Viewmodel
        filtersViewModel.filters.observe(viewLifecycleOwner) { filtersData ->
            val editText = binding.etSearch
            if (editText.text.toString() != filtersData.searchQuery)
                editText.setText(filtersData.searchQuery)
        }

        searchHistoryViewModel.searchHistoriesResult.observe(viewLifecycleOwner) {
            when(it) {
                is ResultUI.Success<List<PropertyFilters>> -> {
                    searchHistoryAdapter.setDateList(it.data)
                     // If no data then show place holder
                    if (it.data.isEmpty()) {
                        binding.tvNoRecentSearchPlaceHolder.visibility = View.VISIBLE
                        binding.rvSearchHistory.visibility = View.GONE
                    }
                    else {
                        binding.tvNoRecentSearchPlaceHolder.visibility = View.GONE
                        binding.rvSearchHistory.visibility = View.VISIBLE
                    }
                }
                ResultUI.Loading -> {

                }
                is ResultUI.Error -> {
                    binding.tvNoRecentSearchPlaceHolder.visibility = View.VISIBLE
                }
            }
        }
    }

    companion object {
        const val IS_NEW_SEARCH = "NEW_SEARCH"
    }
}