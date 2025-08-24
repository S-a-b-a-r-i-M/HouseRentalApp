package com.example.houserentalapp.presentation.utils.extensions

import androidx.fragment.app.Fragment
import com.example.houserentalapp.data.repo.PropertyRepoImpl
import com.example.houserentalapp.domain.usecase.CreatePropertyUseCase
import com.example.houserentalapp.domain.usecase.GetPropertyUseCase
import com.example.houserentalapp.presentation.ui.property.viewmodel.CreatePropertyViewModelFactory
import com.example.houserentalapp.presentation.ui.property.viewmodel.PropertiesListViewModelFactory

fun Fragment.createPropertyViewModelFactory(): CreatePropertyViewModelFactory {
    val useCase = CreatePropertyUseCase(PropertyRepoImpl(requireActivity()))
    return CreatePropertyViewModelFactory(useCase)
}

fun Fragment.createPropertiesListViewModel() : PropertiesListViewModelFactory {
    val useCase = GetPropertyUseCase(PropertyRepoImpl(requireActivity()))
    return PropertiesListViewModelFactory(useCase)
}
