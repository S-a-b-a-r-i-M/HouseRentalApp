package com.example.houserentalapp.presentation.ui.listings

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.houserentalapp.R
import com.example.houserentalapp.data.repo.UserPropertyRepoImpl
import com.example.houserentalapp.databinding.FragmentLeadsBinding
import com.example.houserentalapp.domain.model.Lead
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.usecase.UserPropertyUseCase
import com.example.houserentalapp.presentation.ui.MainActivity
import com.example.houserentalapp.presentation.ui.interfaces.BottomNavController
import com.example.houserentalapp.presentation.ui.listings.adapter.LeadsAdapter
import com.example.houserentalapp.presentation.ui.listings.viewmodel.LeadsViewModel
import com.example.houserentalapp.presentation.ui.listings.viewmodel.LeadsViewModelFactory
import com.example.houserentalapp.presentation.ui.property.viewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.showToast
import com.example.houserentalapp.presentation.utils.helpers.getScrollListener
import kotlin.getValue

// TODO: 1. Add lead created data at UI
class LeadsFragment : Fragment(R.layout.fragment_leads) {
    private lateinit var binding: FragmentLeadsBinding
    private lateinit var bottomNavController: BottomNavController
    private lateinit var currentUser: User
    private lateinit var leadsAdapter: LeadsAdapter

    private lateinit var leadsViewModel: LeadsViewModel
    private val sharedDataViewModel: SharedDataViewModel by activityViewModels()

    private val leadBottomSheet by lazy { LeadBottomSheet() }
    private val _context: Context get() = requireContext()

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
        setupViewModel()
        setupListeners()
        setupObservers()

        // Initial Load
        if (savedInstanceState == null)
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

    private fun setupViewModel() {
        val factory = LeadsViewModelFactory(_context.applicationContext, currentUser)
        leadsViewModel = ViewModelProvider(this, factory)[LeadsViewModel::class]
    }

    private fun onLeadItemClick(lead: Lead) {
        sharedDataViewModel.currentLead = lead
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