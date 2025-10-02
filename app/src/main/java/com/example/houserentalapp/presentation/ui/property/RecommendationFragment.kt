package com.example.houserentalapp.presentation.ui.property

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentRecommandationBinding
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.presentation.model.PropertySummaryUI
import com.example.houserentalapp.presentation.ui.FragmentArgKey
import com.example.houserentalapp.presentation.ui.NavigationDestination
import com.example.houserentalapp.presentation.ui.base.BaseFragment
import com.example.houserentalapp.presentation.ui.property.adapter.PropertiesAdapter
import com.example.houserentalapp.presentation.ui.property.viewmodel.PropertyRecommendationViewModel
import com.example.houserentalapp.presentation.ui.property.viewmodel.PropertyRecommendationViewModelFactory
import com.example.houserentalapp.presentation.ui.sharedviewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.showToast

class RecommendationFragment : BaseFragment(R.layout.fragment_recommandation) {
    private lateinit var binding: FragmentRecommandationBinding
    private lateinit var recommendationViewModel: PropertyRecommendationViewModel
    private lateinit var currentUser: User
    private lateinit var propertiesAdapter: PropertiesAdapter

    private val sharedDataViewModel: SharedDataViewModel by activityViewModels()
    private var propertyId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        propertyId = arguments?.getLong(FragmentArgKey.PROPERTY_ID) ?: 0L
        if (propertyId == 0L) {
            logError("Offset is not found in the args")
            navigationHandler.navigateBack()
            return
        }
        currentUser = sharedDataViewModel.currentUserData
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRecommandationBinding.bind(view)

        setupUI()
        setupViewModels()
        setupObservers()

        if (savedInstanceState == null)
            recommendationViewModel.loadPropertySummaries(propertyId)
    }

    private fun setupUI() {
        propertiesAdapter = PropertiesAdapter(
            ::handleOnPropertyClick, ::handleShortlistToggle
        )
        binding.rvRecommendedProperties.apply {
            layoutManager = LinearLayoutManager(
                _context, RecyclerView.HORIZONTAL, false
            )
            adapter = propertiesAdapter
        }
    }

    private fun setupViewModels() {
        val factory = PropertyRecommendationViewModelFactory(_context.applicationContext, currentUser)
        recommendationViewModel = ViewModelProvider(this, factory)[PropertyRecommendationViewModel::class.java]
    }

    private fun setupObservers() {
        val observer = object : Observer<ResultUI<List<PropertySummaryUI>>> {
            override fun onChanged(value: ResultUI<List<PropertySummaryUI>>) {
                when(value) {
                    is ResultUI.Success<List<PropertySummaryUI>> -> {
                        propertiesAdapter.setDataList(value.data, false)
                    }
                    is ResultUI.Error -> {

                    }
                    ResultUI.Loading -> {

                    }
                }
            }
        }

        recommendationViewModel.propertySummariesResult.observe(viewLifecycleOwner, observer)
    }

    private fun handleOnPropertyClick(propertyId: Long) {
        val bundle = Bundle().apply {
            putLong(FragmentArgKey.PROPERTY_ID, propertyId)
            putBoolean(FragmentArgKey.IS_TENANT_VIEW, true)
            putBoolean(FragmentArgKey.HIDE_AND_SHOW_BOTTOM_NAV, false)
        }

        navigateTo(NavigationDestination.RecommendedSinglePropertyDetail(bundle))
    }

    private fun handleShortlistToggle(propertyId: Long) {
        recommendationViewModel.toggleShortlist(
            propertyId,
            { isShortlisted ->
                val message = if (isShortlisted)
                    "Added to shortlisted"
                else
                    "Removed from shortlisted"
                _context.showToast(message)
            },
            {
                _context.showToast("Retry later")
            }
        )
    }
}