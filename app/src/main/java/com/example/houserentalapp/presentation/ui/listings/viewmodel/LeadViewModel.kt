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
import com.example.houserentalapp.domain.model.enums.LeadUpdatableField
import com.example.houserentalapp.domain.usecase.UserPropertyUseCase
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.extensions.logError
import kotlinx.coroutines.launch

class LeadViewModel(
    private val lead: Lead,
    private val userPropertyUC: UserPropertyUseCase
) : ViewModel() {
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

    private val _leadUIResult = MutableLiveData<Pair<Lead, LeadDirtyFlags>>(
        Pair(lead,
        LeadDirtyFlags (
            leadUserInfoChanged = true,
            statusChanged = true,
            noteChanged = true,
            interestedPropertiesChanged = true
        ))
    )
    val leadUIResult: LiveData<Pair<Lead, LeadDirtyFlags>> = _leadUIResult

    fun clearDirtyFlags() {
        val (_, dirtyFlags) = _leadUIResult.value!!
        dirtyFlags.resetFlags()
    }

    fun updateLeadStatus(
        newStatus: LeadStatus,
        onResult: (Boolean) -> Unit
    ) {
        updateLead(mapOf(LeadUpdatableField.STATUS to newStatus.readable)) {
            onResult(it)
            if (it) {
                val (lead, dirtyFlags) = _leadUIResult.value!!
                dirtyFlags.statusChanged = true
                _leadUIResult.value = Pair(
                    lead.copy(status = newStatus), dirtyFlags
                )
            }
        }
    }

    fun updateLeadNotes(
        newNote: String,
        onResult: (Boolean) -> Unit
    ) {
        updateLead(mapOf(LeadUpdatableField.NOTE to newNote)) {
            onResult(it)
            if (it) {
                val (lead, dirtyFlags) = _leadUIResult.value!!
                dirtyFlags.noteChanged = true
                _leadUIResult.value = Pair(
                    lead.copy(note = newNote), dirtyFlags
                )
            }
        }
    }

    private fun updateLead(
        updateData: Map<LeadUpdatableField, String>,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            when(val res = userPropertyUC.updateLead(lead.id, updateData)) {
                is Result.Success<*> -> {
                    logDebug("Lead update is done. data: $updateData")
                    onResult(true)
                }
                is Result.Error -> {
                    logError("Lead update is failed. exp: ${res.message}", res.exception)
                    onResult(false)
                }
            }
        }
    }
}

class LeadViewModelFactory(
    private val context: Context,
    private val lead: Lead
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LeadViewModel::class.java)) {
            val userPropertyUC = UserPropertyUseCase(UserPropertyRepoImpl(context))
            return LeadViewModel(lead, userPropertyUC) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
