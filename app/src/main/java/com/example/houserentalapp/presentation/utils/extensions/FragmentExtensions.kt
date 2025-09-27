package com.example.houserentalapp.presentation.utils.extensions

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.houserentalapp.data.repo.PropertyRepoImpl
import com.example.houserentalapp.domain.usecase.PropertyUseCase
import com.example.houserentalapp.presentation.ui.base.BaseFragment
import com.example.houserentalapp.presentation.ui.property.viewmodel.CreatePropertyViewModelFactory

fun Fragment.createPropertyViewModelFactory(): CreatePropertyViewModelFactory {
    val useCase = PropertyUseCase(PropertyRepoImpl(requireActivity()))
    return CreatePropertyViewModelFactory(useCase)
}

fun BaseFragment.onBackPressedNavigateBack() {
    requireActivity().onBackPressedDispatcher.addCallback(
        viewLifecycleOwner,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigationHandler.navigateBack()
            }
        }
    )
}