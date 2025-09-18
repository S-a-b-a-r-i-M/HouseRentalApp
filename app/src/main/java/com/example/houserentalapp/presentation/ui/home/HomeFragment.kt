package com.example.houserentalapp.presentation.ui.home

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R
import com.example.houserentalapp.data.repo.SearchHistoryRepoImpl
import com.example.houserentalapp.databinding.FragmentHomeBinding
import com.example.houserentalapp.domain.model.PropertyFilters
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.usecase.SearchHistoryUseCase
import com.example.houserentalapp.presentation.ui.common.SearchViewFragment
import com.example.houserentalapp.presentation.ui.MainActivity
import com.example.houserentalapp.presentation.ui.common.viewmodel.SearchHistoryViewModel
import com.example.houserentalapp.presentation.ui.common.viewmodel.SearchHistoryViewModelFactory
import com.example.houserentalapp.presentation.ui.home.adapter.RecentSearchHistoryAdapter
import com.example.houserentalapp.presentation.ui.property.CreatePropertyFragment
import com.example.houserentalapp.presentation.ui.property.PropertiesListFragment
import com.example.houserentalapp.presentation.ui.property.viewmodel.FiltersViewModel
import com.example.houserentalapp.presentation.ui.property.viewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.showToast

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var mainActivity: MainActivity
    private lateinit var searchHistoryAdapter: RecentSearchHistoryAdapter
    private lateinit var currentUser: User
    private lateinit var searchHistoryViewModel : SearchHistoryViewModel
    private val filtersViewModel: FiltersViewModel by activityViewModels()
    private val sharedDataViewModel: SharedDataViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        // Take Current User
        currentUser = sharedDataViewModel.currentUserData ?: run {
            mainActivity.showToast("Login again...")
            mainActivity.finish()
            return
        }

        setupUI()
        setupViewModel()
        setListeners()
        setupObservers()

        // Load Search Histories
        if (savedInstanceState == null)
            searchHistoryViewModel.loadSearchHistories(currentUser.id)
    }

    private fun setupUI() {
        // Always show bottom nav on HomeFragment
        mainActivity.showBottomNav()

        with(binding) {
            searchHistoryAdapter = RecentSearchHistoryAdapter(::onHistoryClick)
            rvSearchHistory.apply {
                adapter = searchHistoryAdapter
                layoutManager = LinearLayoutManager(
                    mainActivity,
                    RecyclerView.HORIZONTAL,
                    false
                )

            }
        }
    }

    private fun setupViewModel() {
        val uc = SearchHistoryUseCase(SearchHistoryRepoImpl(mainActivity))
        val factory = SearchHistoryViewModelFactory(uc)
        searchHistoryViewModel=ViewModelProvider(this,factory)[SearchHistoryViewModel::class]
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

        mainActivity.loadFragment(PropertiesListFragment(), true)
    }

    private fun setListeners() {
        with(binding) {
            searchBar.setOnClickListener {
                // Let the search view know this is a fresh search
                val destinationFragment = SearchViewFragment()
                destinationFragment.arguments = Bundle().apply {
                    putBoolean(SearchViewFragment.IS_NEW_SEARCH, true)
                }

                mainActivity.loadFragment(destinationFragment, true)
            }

            btnPostProperty.setOnClickListener {
                val destinationFragment = CreatePropertyFragment()
                destinationFragment.arguments = Bundle().apply {
                    putBoolean(CreatePropertyFragment.HIDE_AND_SHOW_BOTTOM_NAV, true)
                }

                mainActivity.loadFragment(destinationFragment, true)
            }
        }
    }

    private fun setupObservers() {
        // Save Changes Into Viewmodel
        searchHistoryViewModel.searchHistoriesResult.observe(viewLifecycleOwner) {
            when(it) {
                is ResultUI.Success<List<PropertyFilters>> -> {
                    searchHistoryAdapter.setDateList(it.data)
                    if (it.data.isEmpty())
                        binding.tvNoRecentSearchPlaceHolder.visibility = View.VISIBLE
                    else
                        binding.tvNoRecentSearchPlaceHolder.visibility = View.GONE
                }
                ResultUI.Loading -> {

                }
                is ResultUI.Error -> {
                    binding.tvNoRecentSearchPlaceHolder.visibility = View.VISIBLE
                }
            }
        }
    }
}