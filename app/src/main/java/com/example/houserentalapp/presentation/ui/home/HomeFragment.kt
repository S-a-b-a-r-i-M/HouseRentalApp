package com.example.houserentalapp.presentation.ui.home

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentHomeBinding
import com.example.houserentalapp.domain.model.PropertyFilters
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.model.UserPropertyStats
import com.example.houserentalapp.presentation.ui.BundleKeys
import com.example.houserentalapp.presentation.ui.NavigationDestination
import com.example.houserentalapp.presentation.ui.base.BaseFragment
import com.example.houserentalapp.presentation.ui.common.viewmodel.SearchHistoryViewModel
import com.example.houserentalapp.presentation.ui.home.adapter.HomeViewModel
import com.example.houserentalapp.presentation.ui.home.adapter.HomeViewModelFactory
import com.example.houserentalapp.presentation.ui.home.adapter.RecentSearchHistoryAdapter
import com.example.houserentalapp.presentation.ui.interfaces.BottomNavController
import com.example.houserentalapp.presentation.ui.sharedviewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding get() = _binding!!
    private lateinit var bottomNavController: BottomNavController
    private lateinit var searchHistoryAdapter: RecentSearchHistoryAdapter
    private lateinit var currentUser: User
    // VIEW MODELS
    private lateinit var homeViewModel: HomeViewModel
    private val searchHistoryViewModel : SearchHistoryViewModel by viewModels()
    private val sharedDataViewModel: SharedDataViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        bottomNavController = context as BottomNavController
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)
        // Take Current User
        currentUser = sharedDataViewModel.currentUserData

        setupUI()
        setupViewModel()
        setListeners()
        setupObservers()

        // Load Search Histories
        if (savedInstanceState == null) {
            searchHistoryViewModel.loadSearchHistories(currentUser.id, 5)
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

            titleTV.apply {
                text = getString(R.string.hi, currentUser.name)
                // contentDescription = "welcome-message"
                contentDescription = getString(R.string.welcome_msg)
            }
        }
    }

    private fun setupViewModel() {
        // val factory1 = SearchHistoryViewModelFactory(_context.applicationContext)
        // searchHistoryViewModel = ViewModelProvider(this)[SearchHistoryViewModel::class]

        val factory2 = HomeViewModelFactory(_context.applicationContext)
        homeViewModel = ViewModelProvider(this,factory2)[HomeViewModel::class]
    }

    private fun onHistoryClick(filters: PropertyFilters) {
        // SET THE SELECTED FILTER
        sharedDataViewModel.setCurrentFilters(filters)

        val bundle = Bundle().apply { putBoolean(BundleKeys.HIDE_BOTTOM_NAV, true) }
        navigateTo(NavigationDestination.PropertyList(bundle))
    }

    private fun setListeners() {
        bottomNavController.showBottomNav()

        with(binding) {
            searchBar.setOnClickListener {
                // This is a new fresh search
                navigateTo(NavigationDestination.SeparateSearch())
            }

            btnPostProperty.setOnClickListener {
                val bundle = Bundle().apply { putLong(BundleKeys.CURRENT_USER_ID, currentUser.id) }
                navigateTo(NavigationDestination.CreateProperty(bundle))
            }

            // STATS CARD LISTENERS
            analyticCardViewed.onItemClick = {
                _context.showToast("Yet to implement")
            }

            analyticCardContacted.onItemClick = {
                _context.showToast("Yet to implement")
            }

            analyticCardShortlisted.onItemClick = {
                navigateTo(NavigationDestination.ShortlistedProperties())
            }

            analyticCardListedProperty.onItemClick = {
                navigateTo(NavigationDestination.MyProperties())
            }

            analyticCardLeads.onItemClick = {
                navigateTo(NavigationDestination.MyLeads())
            }
        }
    }

    private fun bindUserPropertyStats(stats: UserPropertyStats) {
        with(binding) {
            analyticCardViewed.setBadgeCount(stats.viewedPropertyCount)
            analyticCardShortlisted.setBadgeCount(stats.shortlistedPropertyCount)
            analyticCardContacted.setBadgeCount(stats.contactViewedPropertyCount)
            analyticCardListedProperty.setBadgeCount(stats.listedPropertyCount)
            analyticCardLeads.setBadgeCount(stats.leadsCount)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Release the detached view, so it can be immediately collected by CG.
    }
}