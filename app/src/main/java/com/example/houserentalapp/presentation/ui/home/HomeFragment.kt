package com.example.houserentalapp.presentation.ui.home

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentHomeBinding
import com.example.houserentalapp.domain.model.PropertyFilters
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.model.UserPropertyStats
import com.example.houserentalapp.presentation.ui.FragmentArgKey
import com.example.houserentalapp.presentation.ui.NavigationDestination
import com.example.houserentalapp.presentation.ui.base.BaseFragment
import com.example.houserentalapp.presentation.ui.common.viewmodel.SearchHistoryViewModel
import com.example.houserentalapp.presentation.ui.common.viewmodel.SearchHistoryViewModelFactory
import com.example.houserentalapp.presentation.ui.home.adapter.HomeViewModel
import com.example.houserentalapp.presentation.ui.home.adapter.HomeViewModelFactory
import com.example.houserentalapp.presentation.ui.home.adapter.RecentSearchHistoryAdapter
import com.example.houserentalapp.presentation.ui.interfaces.BottomNavController
import com.example.houserentalapp.presentation.ui.property.PropertiesListFragment
import com.example.houserentalapp.presentation.ui.property.viewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.utils.ResultUI

class HomeFragment : BaseFragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var bottomNavController: BottomNavController
    private lateinit var searchHistoryAdapter: RecentSearchHistoryAdapter
    private lateinit var currentUser: User
    // VIEW MODELS
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var searchHistoryViewModel : SearchHistoryViewModel
    private val sharedDataViewModel: SharedDataViewModel by activityViewModels()
    private val _context: Context get() = requireContext()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        bottomNavController = context as BottomNavController
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        // Take Current User
        currentUser = sharedDataViewModel.currentUserData

        setupUI()
        setupViewModel()
        setListeners()
        setupObservers()

        // Load Search Histories
        if (savedInstanceState == null) {
            searchHistoryViewModel.loadSearchHistories(currentUser.id)
            homeViewModel.loadUserPropertyStats(currentUser.id)
        }
    }

    private fun setupUI() {
        // Always show bottom nav on HomeFragment
        bottomNavController.showBottomNav()

        with(binding) {
            // Recycler View
            searchHistoryAdapter = RecentSearchHistoryAdapter(::onHistoryClick)
            rvSearchHistory.apply {
                adapter = searchHistoryAdapter
                layoutManager = LinearLayoutManager(
                    context,
                    RecyclerView.HORIZONTAL,
                    false
                )
            }

            titleTV.text = getString(R.string.hi, currentUser.name)
        }
    }

    private fun setupViewModel() {
        val factory1 = SearchHistoryViewModelFactory(_context.applicationContext)
        searchHistoryViewModel=ViewModelProvider(this,factory1)[SearchHistoryViewModel::class]

        val factory2 = HomeViewModelFactory(_context.applicationContext)
        homeViewModel = ViewModelProvider(this,factory2)[HomeViewModel::class]
    }

    private fun onHistoryClick(filters: PropertyFilters) {
        // SET THE SELECTED FILTER
        sharedDataViewModel.setCurrentFilters(filters)

        val bundle = Bundle().apply { putBoolean(PropertiesListFragment.HIDE_BOTTOM_NAV_KEY, true) }
        navigateTo(NavigationDestination.PropertyList(bundle))
    }

    private fun setListeners() {
        with(binding) {
            searchBar.setOnClickListener {
                // Let the search view know this is a fresh search
                val bundle = Bundle().apply { putBoolean(FragmentArgKey.IS_NEW_SEARCH, true) }
                navigateTo(NavigationDestination.SeparateSearch(bundle))
            }

            btnPostProperty.setOnClickListener {
                val bundle = Bundle().apply {
                    putBoolean(FragmentArgKey.HIDE_AND_SHOW_BOTTOM_NAV, true)
                }
                navigateTo(NavigationDestination.CreateProperty(bundle))
            }
        }
    }

    private fun bindUserPropertyStats(stats: UserPropertyStats) {
        with(binding) {
            analyticCardViewed.setBadgeCount(stats.viewedPropertyCount)
            analyticCardShortlisted.setBadgeCount(stats.shortlistedPropertyCount)
            analyticCardContacted.setBadgeCount(stats.contactViewedPropertyCount)
            analyticCardListedProperty.setBadgeCount(stats.listedPropertyCount)
            analyticCardLeadsCount.setBadgeCount(stats.leadsCount)
        }
    }

    private fun setupObservers() {
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

        homeViewModel.userPropertyStatsResult.observe(viewLifecycleOwner) {
            when(it) {
                is ResultUI.Success<UserPropertyStats> -> {
                    bindUserPropertyStats(it.data)
                }
                is ResultUI.Error -> {

                }
                ResultUI.Loading -> {

                }
            }
        }
    }
}