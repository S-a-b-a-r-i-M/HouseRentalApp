package com.example.houserentalapp.presentation.ui.listings

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentLeadBottomSheetBinding
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.model.enums.LeadStatus
import com.example.houserentalapp.presentation.ui.FragmentArgKey
import com.example.houserentalapp.presentation.ui.listings.adapter.LeadInterestedPropertiesAdapter
import com.example.houserentalapp.presentation.ui.listings.viewmodel.LeadViewModel
import com.example.houserentalapp.presentation.ui.listings.viewmodel.LeadViewModelFactory
import com.example.houserentalapp.presentation.ui.listings.viewmodel.LeadWithFlags
import com.example.houserentalapp.presentation.ui.property.viewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.showToast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LeadBottomSheet
    : BottomSheetDialogFragment(R.layout.fragment_lead_bottom_sheet) {
    private lateinit var binding: FragmentLeadBottomSheetBinding
    private lateinit var currentUser: User
    private lateinit var adapter: LeadInterestedPropertiesAdapter
    private lateinit var leadViewModel: LeadViewModel
    private val sharedDataViewModel: SharedDataViewModel by activityViewModels()
    private val _context: Context get() = requireContext()
    private var leadId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        leadId = arguments?.getLong(FragmentArgKey.LEAD_ID) ?: 0L
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLeadBottomSheetBinding.bind(view)
        // Take Current User
        currentUser = sharedDataViewModel.currentUserData

        setupUI()
        setupViewModel()
        setupListeners()
        setupObservers()

         if (savedInstanceState == null) {
             if (leadId == 0L) {
                 logError("Lead id is not found")
                 dismiss() // Close
             }
             else
                 leadViewModel.loadLead(leadId)
         }
    }

    private fun setupUI() {
        with(binding) {
            // Recycler View
            adapter = LeadInterestedPropertiesAdapter(::handleStatusChange)
            rvLeadInterestedProperties.apply {
                layoutManager = LinearLayoutManager(_context)
                this.adapter = this@LeadBottomSheet.adapter
            }
        }
    }

    private fun bindLeadDetails(leadUser: User) {
        with(binding) {
            tvLeadName.text = leadUser.name
            tvLeadPhone.text = leadUser.phone
            if (leadUser.email != null) {
                tvLeadEmail.visibility = View.VISIBLE
                tvLeadEmail.text = leadUser.email
            } else
                tvLeadEmail.visibility = View.GONE
        }
    }

    private fun setupViewModel() {
        val factory = LeadViewModelFactory(_context.applicationContext)
        leadViewModel = ViewModelProvider(this, factory)[LeadViewModel::class]
    }

    private fun setupListeners() {
        binding.inlineEditNote.onValueChanged = ::handleLeadNoteChange

        @SuppressLint("ClickableViewAccessibility")
        binding.rvLeadInterestedProperties.setOnTouchListener { v, event ->
            v.parent?.requestDisallowInterceptTouchEvent(true)
            false
        }
    }

    private fun handleLeadNoteChange(newNote: String) {
        leadViewModel.updateLeadNotes(newNote) { isSuccess ->
            val msg = if (isSuccess) "Note updated successfully." else "Note update failed."
            _context.showToast(msg)
        }
    }

    private fun handleStatusChange(propertyId: Long, newStatus: LeadStatus) {
        leadViewModel.updateLeadPropertyStatus(propertyId, newStatus) { isSuccess ->
            val msg = if (isSuccess) "Status updated successfully." else "Status update failed."
            _context.showToast(msg)
        }
    }

    private fun setupObservers() {
        leadViewModel.leadUIResult.observe(viewLifecycleOwner) {
            when(it) {
                is ResultUI.Success<LeadWithFlags> -> {
                    val (lead, dirtyFlags) = it.data
                    if (dirtyFlags.leadUserInfoChanged)
                        bindLeadDetails(lead.leadUser)

                    if (dirtyFlags.noteChanged)
                        binding.inlineEditNote.setValue(lead.note ?: "")

                    if (dirtyFlags.interestedPropertiesChanged)
                        adapter.setDataList(lead.interestedPropertiesWithStatus)

                    // CLEAR THE FLAGS
                    leadViewModel.clearDirtyFlags()
                }
                is ResultUI.Error -> {
                    _context.showToast("Unexpected error occurred, Try again later.")
                    logError(it.message)
                }
                ResultUI.Loading -> {}
            }
        }
    }
}