package com.example.houserentalapp.presentation.ui.common

import android.os.Bundle
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.houserentalapp.R
import com.example.houserentalapp.data.repo.SearchHistoryRepoImpl
import com.example.houserentalapp.databinding.FragmentFiltersBinding
import com.example.houserentalapp.domain.model.PropertyFilters
import com.example.houserentalapp.domain.usecase.SearchHistoryUseCase
import com.example.houserentalapp.presentation.ui.MainActivity
import com.example.houserentalapp.presentation.ui.common.adapter.SearchHistoryAdapter
import com.example.houserentalapp.presentation.ui.common.viewmodel.SearchHistoryViewModel
import com.example.houserentalapp.presentation.ui.common.viewmodel.SearchHistoryViewModelFactory
import com.example.houserentalapp.presentation.ui.property.PropertiesListFragment
import com.example.houserentalapp.presentation.ui.property.viewmodel.FiltersViewModel
import com.example.houserentalapp.presentation.ui.property.viewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.helpers.setSystemBarBottomPadding
import com.google.android.material.search.SearchView

class SearchViewFragment : Fragment(R.layout.fragment_filters) {
    private lateinit var binding: FragmentFiltersBinding
    private lateinit var mainActivity: MainActivity
    private lateinit var searchHistoryAdapter: SearchHistoryAdapter
    private lateinit var searchHistoryViewModel : SearchHistoryViewModel
    private val sharedDataViewModel: SharedDataViewModel by activityViewModels()
    private val filtersViewModel: FiltersViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFiltersBinding.bind(view)
        mainActivity = context as MainActivity

        val isNewSearch = arguments?.getBoolean(IS_NEW_SEARCH) ?: false
        if (isNewSearch) {
            // Reset Filters on the first creation of this fragment(hence used arguments)
            logDebug("isNewSearch given as true so all the previous filters are cleared")
            filtersViewModel.resetFilters()
        }

        setupUI()
        setupViewModel()
        setupListeners()
        setupObservers()

        // Load Search Histories
        if (searchHistoryViewModel.searchHistoriesResult.value !is ResultUI.Success)
            searchHistoryViewModel.loadSearchHistories(mainActivity.getCurrentUser().id)
    }

    private fun setupUI() {
        // Always hide bottom nav on this Fragment
        mainActivity.hideBottomNav()

        // Add paddingBottom to avoid system bar overlay
        setSystemBarBottomPadding(binding.root)

        with(binding) {
            // Recycler View
            searchHistoryAdapter = SearchHistoryAdapter(::onHistoryClick)
            rvSearchHistory.apply {
                adapter = searchHistoryAdapter
                layoutManager = LinearLayoutManager(mainActivity)
            }

            // Search View
            searchView.show()
            updateSubmitButtonState(searchView.editText.text.toString().length > 2)
        }
    }

    private fun updateSubmitButtonState(enable: Boolean) {
        val button = binding.btnSubmit
        if (enable == button.isEnabled) return

        if (enable)
            binding.btnSubmit.apply {
                isEnabled = true
                alpha = 1f
            }
        else
            binding.btnSubmit.apply {
                isEnabled = false
                alpha = 0.7f
            }
    }

    private fun setupViewModel() {
        val uc = SearchHistoryUseCase(SearchHistoryRepoImpl(mainActivity))
        val factory = SearchHistoryViewModelFactory(uc)
        searchHistoryViewModel = ViewModelProvider(this, factory)
            .get(SearchHistoryViewModel::class.java)
    }

    private fun onHistoryClick(filters: PropertyFilters) {
        filtersViewModel.setPropertyFilters(filters)
        navigateToPropertiesListFragment()
    }

    private fun navigateToPropertiesListFragment() {
        sharedDataViewModel.resetPropertiesListStore()
        sharedDataViewModel.addToPropertiesListStore(
            PropertiesListFragment.Companion.HIDE_BOTTOM_NAV_KEY, true
        )

        mainActivity.loadFragment(
            PropertiesListFragment(),
            true
        )
    }

    private fun setupListeners() {
        with(binding) {

            searchView.addTransitionListener { view, pervState, newState ->
                when(newState) {
                    SearchView.TransitionState.SHOWING -> {}
                    SearchView.TransitionState.SHOWN -> {
                        logDebug("SearchView.TransitionState.SHOWN")
                    }
                    SearchView.TransitionState.HIDING -> {}
                    SearchView.TransitionState.HIDDEN -> {
                        logDebug("SearchView.TransitionState.HIDDEN")
                        parentFragmentManager.popBackStack()
                    }
                }
            }

            // Setup SearchView text changes
            searchView.editText.doOnTextChanged { text, start, before, count ->
                val query = text?.toString() ?: ""
                filtersViewModel.setSearchQuery(query)
                updateSubmitButtonState(query.length > 2)
                // filter search histories
                // search locations
            }

            btnSubmit.setOnClickListener { navigateToPropertiesListFragment() }
        }
    }

    private fun setupObservers() {
        // Save Changes Into Viewmodel
        filtersViewModel.filters.observe(viewLifecycleOwner) { filtersData ->
            val editText = binding.searchView.editText
            if (editText.text.toString() != filtersData.searchQuery)
                editText.setText(filtersData.searchQuery)
        }

        searchHistoryViewModel.searchHistoriesResult.observe(viewLifecycleOwner) {
            when(it) {
                is ResultUI.Success<List<PropertyFilters>> -> {
                    searchHistoryAdapter.setDateList(it.data)
                }
                ResultUI.Loading -> {

                }
                is ResultUI.Error -> {

                }
            }
        }
    }

    override fun onDetach() {
        mainActivity.showBottomNav()
        super.onDetach()
    }

    companion object {
        const val IS_NEW_SEARCH = "NEW_SEARCH"
    }
}