package com.example.houserentalapp.presentation.ui.property.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.houserentalapp.domain.model.Property
import com.example.houserentalapp.domain.usecase.GetPropertyUseCase
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import kotlinx.coroutines.launch

class SinglePropertyDetailViewModel(private val useCase: GetPropertyUseCase) : ViewModel() {
    private val _propertyResult = MutableLiveData<ResultUI<Property>>()
    val propertyResult: LiveData<ResultUI<Property>> = _propertyResult

    fun loadProperty(propertyId: Long) {
        _propertyResult.value = ResultUI.Loading
        viewModelScope.launch {
            try {
                when (val result = useCase.getProperty(propertyId)) {
                    is Result.Success<Property> -> {
                        _propertyResult.value = ResultUI.Success(result.data)
                        logInfo("loadProperty -> successfully fetched property(id: $propertyId)")
                    }
                    is Result.Error -> {
                        _propertyResult.value = ResultUI.Error(result.message)
                        logError("Error on loadProperty : ${result.message}")
                    }
                }
            } catch (exp: Exception) {
                _propertyResult.value = ResultUI.Error("Unexpected Error")
                logError("Error on loadProperty : ${exp.message}")
            }
        }
    }
}

class SinglePropertyDetailViewModelFactory(
    private val useCase: GetPropertyUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SinglePropertyDetailViewModel::class.java))
            return SinglePropertyDetailViewModel(useCase) as T

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}