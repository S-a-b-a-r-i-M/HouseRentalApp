package com.example.houserentalapp.presentation.ui.home.adapter

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.houserentalapp.data.repo.UserPropertyRepoImpl
import com.example.houserentalapp.domain.model.UserPropertyStats
import com.example.houserentalapp.domain.usecase.UserPropertyUseCase
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import kotlinx.coroutines.launch

class HomeViewModel(private val userPropertyUC: UserPropertyUseCase) : ViewModel() {
    private val _userPropertyStatsResult = MutableLiveData<ResultUI<UserPropertyStats>>()
    val userPropertyStatsResult: LiveData<ResultUI<UserPropertyStats>> = _userPropertyStatsResult

    fun loadUserPropertyStats(userId: Long) {
        viewModelScope.launch {
            _userPropertyStatsResult.value = ResultUI.Loading
            when(val result = userPropertyUC.getUserPropertyStats(userId)) {
                is Result.Success<UserPropertyStats> -> {
                    logInfo("User's stats loaded successfully")
                    _userPropertyStatsResult.value = ResultUI.Success(result.data)
                }
                is Result.Error -> {
                    logError("Error occurred while fetching user stats ${result.message}")
                    _userPropertyStatsResult.value = ResultUI.Error(result.message)
                }
            }
        }
    }
}

class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            val userPropertyUC = UserPropertyUseCase(UserPropertyRepoImpl(context))
            return HomeViewModel(userPropertyUC) as T
        }

        throw IllegalArgumentException("SearchHistoryViewModelFactory: Unknown ViewModel class")
    }
}