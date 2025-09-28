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

    fun updateLeadPropertyStatus(
        propertyId: Long,
        newStatus: LeadStatus,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            when(val res = userPropertyUC.updateLeadPropertyStatus(
                lead.id, propertyId, newStatus)
            ) {
                is Result.Success<*> -> {
                    val idx = lead.interestedPropertiesWithStatus.indexOfFirst { it.first.id == propertyId }
                    if (idx == -1) {
                        logError("property index is missing")
                        onResult(false)
                        return@launch
                    }
                    val newList = lead.interestedPropertiesWithStatus.toMutableList()
                    newList[idx] = lead.interestedPropertiesWithStatus[idx].copy(second = newStatus)
                    val (lead, dirtyFlags) = _leadUIResult.value!!
                    dirtyFlags.interestedPropertiesChanged = true
                    _leadUIResult.value = Pair(
                        lead.copy(interestedPropertiesWithStatus = newList),
                        dirtyFlags
                    )
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
            when(val res = userPropertyUC.updateLeadNotes(lead.id, newNote)) {
                is Result.Success<*> -> {
                    val (lead, dirtyFlags) = _leadUIResult.value!!
                    dirtyFlags.noteChanged = true
                    _leadUIResult.value = Pair(lead.copy(note = newNote), dirtyFlags)
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
