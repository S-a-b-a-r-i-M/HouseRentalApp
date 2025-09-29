package com.example.houserentalapp.presentation.ui.listings.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.houserentalapp.data.repo.UserPropertyRepoImpl
import com.example.houserentalapp.domain.model.Lead
import com.example.houserentalapp.domain.model.enums.LeadStatus
import com.example.houserentalapp.domain.usecase.UserPropertyUseCase
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.ui.listings.viewmodel.LeadViewModel.LeadDirtyFlags
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.extensions.logError
import kotlinx.coroutines.launch

typealias LeadWithFlags = Pair<Lead, LeadDirtyFlags>

class LeadViewModel(private val userPropertyUC: UserPropertyUseCase) : ViewModel() {
    data class LeadDirtyFlags (
        var leadUserInfoChanged: Boolean = false,
        var statusChanged: Boolean = false,
        var noteChanged: Boolean = false,
        var interestedPropertiesChanged: Boolean = false,
    ) {
        fun resetFlags() {
            leadUserInfoChanged = false
            statusChanged = false
            noteChanged = false
            interestedPropertiesChanged = false
        }
    }

    private val _leadUIResult = MutableLiveData<ResultUI<LeadWithFlags>>()
    val leadUIResult: LiveData<ResultUI<LeadWithFlags>> = _leadUIResult
    private lateinit var leadWithFlags: LeadWithFlags

    fun clearDirtyFlags() {
        val (_, dirtyFlags) = leadWithFlags
        dirtyFlags.resetFlags()
    }

    fun loadLead(leadId: Long) {
        viewModelScope.launch {
            when(val res = userPropertyUC.getLead(leadId)) {
                is Result.Success<Lead> -> {
                    leadWithFlags = Pair(
                        res.data,
                        LeadDirtyFlags().apply {
                            leadUserInfoChanged = true
                            statusChanged = true
                            noteChanged = true
                            interestedPropertiesChanged = true
                        }
                    )
                    _leadUIResult.value = ResultUI.Success(leadWithFlags)
                    logDebug("Lead fetched is done.")
                }
                is Result.Error -> {
                    logError("Lead loadLead is failed. exp: ${res.message}", res.exception)
                }
            }
        }
    }

    fun updateLeadPropertyStatus(
        propertyId: Long,
        newStatus: LeadStatus,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val (lead, dirtyFlags) = leadWithFlags
            when(val res = userPropertyUC.updateLeadPropertyStatus(
                lead.id, propertyId, newStatus)
            ) {
                is Result.Success<*> -> {
                    val idx = lead.interestedPropertiesWithStatus.indexOfFirst { it.first.id == propertyId }
                    if (idx == -1) {
                        logError("property index is missing for propertyId($propertyId")
                        onResult(false)
                        return@launch
                    }
                    val newList = lead.interestedPropertiesWithStatus.toMutableList()
                    newList[idx] = lead.interestedPropertiesWithStatus[idx].copy(second = newStatus)
                    dirtyFlags.interestedPropertiesChanged = true
                    leadWithFlags = Pair(
                        lead.copy(interestedPropertiesWithStatus = newList),
                        dirtyFlags
                    )
                    _leadUIResult.value = ResultUI.Success(leadWithFlags)
                    logDebug("Lead update is done.")
                    onResult(true)
                }
                is Result.Error -> {
                    logError("Lead update is failed. exp: ${res.message}", res.exception)
                    onResult(false)
                }
            }
        }
    }

    fun updateLeadNotes(newNote: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val (lead, dirtyFlags) = leadWithFlags
            when(val res = userPropertyUC.updateLeadNotes(lead.id, newNote)) {
                is Result.Success<*> -> {
                    dirtyFlags.noteChanged = true
                    leadWithFlags = Pair(lead.copy(note = newNote), dirtyFlags)
                    _leadUIResult.value = ResultUI.Success(leadWithFlags)
                    logDebug("Lead note update is done.")
                    onResult(true)
                }
                is Result.Error -> {
                    logError("Lead note update is failed. exp: ${res.message}", res.exception)
                    onResult(false)
                }
            }
        }
    }
}

class LeadViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LeadViewModel::class.java)) {
            val userPropertyUC = UserPropertyUseCase(UserPropertyRepoImpl(context))
            return LeadViewModel(userPropertyUC) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
