package com.example.houserentalapp.presentation.utils.extensions

import androidx.fragment.app.Fragment
import com.example.houserentalapp.data.repo.PropertyRepoImpl
import com.example.houserentalapp.domain.usecase.CreatePropertyUseCase
import com.example.houserentalapp.presentation.ui.property.viewmodel.CreatePropertyViewModelFactory

fun Fragment.createPropertyViewModelFactory(): CreatePropertyViewModelFactory {
    val useCase = CreatePropertyUseCase(PropertyRepoImpl(requireActivity()))
    return CreatePropertyViewModelFactory(useCase)
}
