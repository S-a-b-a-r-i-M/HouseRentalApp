package com.example.houserentalapp.presentation.ui.common.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.houserentalapp.data.repo.SearchHistoryRepoImpl
import com.example.houserentalapp.domain.model.PropertyFilters
import com.example.houserentalapp.domain.usecase.SearchHistoryUseCase
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import kotlinx.coroutines.launch

class SearchHistoryViewModel(private val searchHistoryUC: SearchHistoryUseCase) : ViewModel() {
    private val _searchHistoriesResult = MutableLiveData<ResultUI<List<PropertyFilters>>>(null)
    val searchHistoriesResult: LiveData<ResultUI<List<PropertyFilters>>> = _searchHistoriesResult
    fun loadSearchHistories(userId: Long, limit: Int = 15) {
        viewModelScope.launch {
            _searchHistoriesResult.value = ResultUI.Loading
            when(val result = searchHistoryUC.getResentSearchHistories(userId, limit)) {
                is Result.Success<List<PropertyFilters>> -> {
                    logInfo("Search history(${result.data.size} loaded successfully")
                    _searchHistoriesResult.value = ResultUI.Success(result.data)
                }
                is Result.Error -> {
                    logError("Error occurred while fetching search histories ${result.message}")
                    _searchHistoriesResult.value = ResultUI.Error(result.message)
                }
            }
        }
    }
}

class SearchHistoryViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchHistoryViewModel::class.java)) {
            val searchHistoryUC = SearchHistoryUseCase(SearchHistoryRepoImpl(context))
            return SearchHistoryViewModel(searchHistoryUC) as T
        }

        throw IllegalArgumentException("SearchHistoryViewModelFactory: Unknown ViewModel class")
    }
}