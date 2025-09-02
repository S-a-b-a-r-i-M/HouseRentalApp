package com.example.houserentalapp.presentation.ui.property.viewmodel

import androidx.lifecycle.ViewModel

class SharedDataViewModel : ViewModel() {

    // TODO: Make it private
    val fPropertiesListMap = mutableMapOf<String, Any>()
    val fSinglePropertyDetailMap = mutableMapOf<String, Any>()
    val fCreatePropertyMap = mutableMapOf<String, Any>()
}