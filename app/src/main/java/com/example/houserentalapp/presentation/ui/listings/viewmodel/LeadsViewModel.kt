package com.example.houserentalapp.presentation.ui.listings.viewmodel
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.houserentalapp.data.repo.UserPropertyRepoImpl
import com.example.houserentalapp.domain.model.Lead
import com.example.houserentalapp.domain.model.Pagination
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.usecase.UserPropertyUseCase
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logError
import kotlinx.coroutines.launch
import com.example.houserentalapp.domain.utils.Result

class LeadsViewModel(
    private val userPropertyUC: UserPropertyUseCase, private val currentUser: User
) : ViewModel() {
    private val _leadsResult = MutableLiveData<ResultUI<List<Lead>>>()
    val leadsResult: LiveData<ResultUI<List<Lead>>> = _leadsResult
    private val leads: MutableList<Lead> = mutableListOf()

    var hasMore: Boolean = true
        private set
    private var offset: Int = 0
    private val limit: Int = 10

    fun loadLeads() {
        if (!hasMore) return
        _leadsResult.value = ResultUI.Loading
        viewModelScope.launch {
            try {
                val result = userPropertyUC.getLeadsByLandlord(
                    currentUser.id,
                    Pagination(offset, limit),
                )

                when (result) {
                    is Result.Success<List<Lead>> -> {
                        leads.addAll(result.data)
                        _leadsResult.value = ResultUI.Success(leads)

                        // Update Offset & HasMore
                        offset += limit
                        hasMore = result.data.size == limit
                    }
                    is Result.Error -> {
                        logError("Error on loadLeads : ${result.message}")
                        _leadsResult.value = ResultUI.Error(result.message)
                    }
                }
            } catch (exp: Exception) {
                logError("Error on loadLeads : ${exp.message}")
                _leadsResult.value = ResultUI.Error("Unexpected Error")
            }
        }
    }
}

class LeadsViewModelFactory(
    private val context: Context,
    private val currentUser: User
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LeadsViewModel::class.java)) {
            val userPropertyUC = UserPropertyUseCase(UserPropertyRepoImpl(context))
            return LeadsViewModel(userPropertyUC, currentUser) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
