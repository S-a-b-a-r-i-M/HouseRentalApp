package com.example.houserentalapp.presentation.ui.listings

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentLeadsBinding
import com.example.houserentalapp.domain.model.Lead
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.presentation.ui.BundleKeys
import com.example.houserentalapp.presentation.ui.base.BaseFragment
import com.example.houserentalapp.presentation.ui.interfaces.BottomNavController
import com.example.houserentalapp.presentation.ui.listings.adapter.LeadsAdapter
import com.example.houserentalapp.presentation.ui.listings.viewmodel.LeadsViewModel
import com.example.houserentalapp.presentation.ui.sharedviewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.onBackPressedNavigateBack
import com.example.houserentalapp.presentation.utils.helpers.getScrollListener

// TODO: 1. Add lead created data at UI
class LeadsFragment : BaseFragment(R.layout.fragment_leads) {
    private lateinit var binding: FragmentLeadsBinding
    private lateinit var bottomNavController: BottomNavController
    private lateinit var currentUser: User
    private lateinit var leadsAdapter: LeadsAdapter

    private val leadsViewModel: LeadsViewModel by viewModels({ requireParentFragment() })
    private val sharedDataViewModel: SharedDataViewModel by activityViewModels()

    private val leadBottomSheet by lazy { LeadBottomSheet() }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        bottomNavController = context as BottomNavController
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLeadsBinding.bind(view)
        // Take Current User
        currentUser = sharedDataViewModel.currentUserData

        setupUI()
        setupListeners()
        setupObservers()
        onBackPressedNavigateBack()

        // Initial Load
        if (leadsViewModel.leadsResult.value !is ResultUI.Success)
            leadsViewModel.loadLeads()
    }

    private fun setupUI() {
        with(binding) {
            // Recycler View
            leadsAdapter = LeadsAdapter(::onLeadItemClick)

            rvLeads.apply {
                adapter = leadsAdapter
                layoutManager = LinearLayoutManager(_context)
                val scrollListener = getScrollListener(
                    { leadsViewModel.hasMore },
                    { leadsViewModel.loadLeads() }
                )
                addOnScrollListener(scrollListener)
            }
        }
    }

    private fun onLeadItemClick(lead: Lead) {
        leadBottomSheet.arguments = Bundle().apply { putLong(BundleKeys.LEAD_ID, lead.id) }
        leadBottomSheet.show(parentFragmentManager, "LeadBottomSheet")
    }

    private fun setupListeners() {

    }

    private fun onLeadsDataLoaded(leads: List<Lead>) {
        leadsAdapter.setLeads(leads)
        // PlaceHolder
        val noDataPlaceHolderView = binding.noDataPlaceHolderView.root
        if (leadsAdapter.itemCount == 0) {
            noDataPlaceHolderView.visibility = View.VISIBLE
            noDataPlaceHolderView.findViewById<TextView>(R.id.tvNoDataMsg)?.let { textView ->
                textView.text = getString(R.string.no_leads_found)
            }
        }
        else {
            noDataPlaceHolderView.visibility = View.GONE
        }
    }

    private fun setupObservers() {
        leadsViewModel.leadsResult.observe(viewLifecycleOwner) { resultUI ->
            when(resultUI) {
                is ResultUI.Success<List<Lead>> -> {
                    onLeadsDataLoaded(resultUI.data)
                }
                ResultUI.Loading -> {

                }
                is ResultUI.Error -> {

                }
            }
        }
    }
}