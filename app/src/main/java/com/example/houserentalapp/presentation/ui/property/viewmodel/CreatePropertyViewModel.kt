package com.example.houserentalapp.presentation.ui.property.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.houserentalapp.domain.model.AmenityDomain
import com.example.houserentalapp.domain.model.ImageSource
import com.example.houserentalapp.domain.model.Property
import com.example.houserentalapp.domain.model.PropertyAddress
import com.example.houserentalapp.domain.model.PropertyImage
import com.example.houserentalapp.domain.model.enums.AmenityType
import com.example.houserentalapp.domain.model.enums.BHK
import com.example.houserentalapp.domain.model.enums.BachelorType
import com.example.houserentalapp.domain.model.enums.CountableInternalAmenity
import com.example.houserentalapp.domain.model.enums.FurnishingType
import com.example.houserentalapp.domain.model.enums.InternalAmenity
import com.example.houserentalapp.domain.model.enums.PropertyKind
import com.example.houserentalapp.domain.model.enums.PropertyType
import com.example.houserentalapp.domain.model.enums.SocialAmenity
import com.example.houserentalapp.domain.model.enums.TenantType
import com.example.houserentalapp.domain.usecase.PropertyUseCase
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.enums.PropertyFormField
import com.example.houserentalapp.presentation.model.PropertyAddressUI
import com.example.houserentalapp.presentation.model.PropertyBasicUI
import com.example.houserentalapp.presentation.model.PropertyPreferencesUI
import com.example.houserentalapp.presentation.model.PropertyPricingUI
import com.example.houserentalapp.presentation.utils.helpers.toEpochSeconds
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.logWarning
import kotlinx.coroutines.launch

class CreatePropertyViewModel(
    private val propertyUseCase: PropertyUseCase
) : ViewModel() {
    private val _createPropertyResult = MutableLiveData<ResultUI<Long>?>()
    val createPropertyResult: LiveData<ResultUI<Long>?> = _createPropertyResult

    // Property Data Models
    private val _propertyBasicUI = MutableLiveData((PropertyBasicUI()))
    val propertyBasicUI: LiveData<PropertyBasicUI> = _propertyBasicUI
    private val _propertyPreferencesUI = MutableLiveData(PropertyPreferencesUI())
    val propertyPreferencesUI: LiveData<PropertyPreferencesUI> = _propertyPreferencesUI
    private val _propertyPricingUI = MutableLiveData(PropertyPricingUI())
    val propertyPricingUI: LiveData<PropertyPricingUI> = _propertyPricingUI
    private val _propertyAddressUI = MutableLiveData(PropertyAddressUI())
    val propertyAddressUI: LiveData<PropertyAddressUI> = _propertyAddressUI

    // Images
    private val _imageUris = MutableLiveData<List<Uri>>(emptyList())
    val imageUris: LiveData<List<Uri>> = _imageUris

    // Amenities
    private val _icAmenityMap = MutableLiveData<Map<CountableInternalAmenity, Int>>(emptyMap())
    val icAmenityMap: LiveData<Map<CountableInternalAmenity, Int>> = _icAmenityMap
    private val _internalAmenitySet = MutableLiveData<Set<InternalAmenity>>(emptySet())
    val internalAmenitySet: LiveData<Set<InternalAmenity>> = _internalAmenitySet
    private val _socialAmenitySet = MutableLiveData<Set<SocialAmenity>>(emptySet())
    val socialAmenitySet: LiveData<Set<SocialAmenity>> = _socialAmenitySet

    private val formErrorMap = PropertyFormField.entries.filter { it.isRequired }.associateWith {
        MutableLiveData<String?>(null)
    }

    private val _validationError = MutableLiveData<String?>()
    val validationError: LiveData<String?> = _validationError

    fun getFormErrorMap(field: PropertyFormField) : LiveData<String?> = formErrorMap.getValue(field)

    private fun updateFormFieldError(field: PropertyFormField) {
        val formFieldErr = formErrorMap.getValue(field)
        if (formFieldErr.value != null) formFieldErr.value = null
    }

    private fun updatePropertyBasic(field: PropertyFormField, update: (PropertyBasicUI) -> PropertyBasicUI) {
        val currentValue = _propertyBasicUI.value!!
        _propertyBasicUI.value = update(currentValue)
        if (field.isRequired)
            updateFormFieldError(field)
    }

    private fun updatePreferences(field: PropertyFormField, update: (PropertyPreferencesUI) -> PropertyPreferencesUI) {
        val currentValue = _propertyPreferencesUI.value!!
        _propertyPreferencesUI.value = update(currentValue)
        if (field.isRequired)
            updateFormFieldError(field)
    }

    private fun updatePricing(field: PropertyFormField, update: (PropertyPricingUI) -> PropertyPricingUI) {
        val currentValue = _propertyPricingUI.value!!
        _propertyPricingUI.value = update(currentValue)
        if (field.isRequired)
            updateFormFieldError(field)
    }

    private fun updateAddress(field: PropertyFormField, update: (PropertyAddressUI) -> PropertyAddressUI) {
        val currentValue = _propertyAddressUI.value!!
        _propertyAddressUI.value = update(currentValue)
        if (field.isRequired)
            updateFormFieldError(field)
    }

    fun updateInternalCountableAmenity(amenity: CountableInternalAmenity, updateValue: Int) {
        val mutableMap = _icAmenityMap.value!!.toMutableMap()
        val newValue = mutableMap.getOrDefault(amenity, 0) + updateValue
        if (newValue == 0)
            mutableMap.remove(amenity)
        else
            mutableMap.put(amenity, newValue)

        _icAmenityMap.value = mutableMap
    }

    fun updateFormValue(field: PropertyFormField, value: String) {
        when (field) {
            PropertyFormField.NAME -> updatePropertyBasic(field) { it.copy(name = value) }
            PropertyFormField.DESCRIPTION -> updatePropertyBasic(field) { it.copy(description = value) }
            PropertyFormField.STREET -> updateAddress(field) { it.copy(street = value)}
            PropertyFormField.LOCALITY -> updateAddress(field) { it.copy(locality = value)}
            PropertyFormField.CITY -> updateAddress(field) { it.copy(city = value)}
            PropertyFormField.PRICE -> updatePricing(field) { it.copy(price = value) }
            PropertyFormField.MAINTENANCE_CHARGES -> updatePricing(field) { it.copy(maintenanceCharges = value) }
            PropertyFormField.BUILT_UP_AREA -> updatePropertyBasic(field) { it.copy(builtUpArea = value) }
            PropertyFormField.SECURITY_DEPOSIT -> updatePricing(field) { it.copy(securityDepositAmount = value) }
            PropertyFormField.BATH_ROOM_COUNT -> updatePropertyBasic(field) { it.copy(bathRoomCount = value) }
            PropertyFormField.COVERED_PARKING_COUNT -> updatePreferences(field) { it.copy(countOfCoveredParking = value) }
            PropertyFormField.OPEN_PARKING_COUNT -> updatePreferences(field) { it.copy(countOfOpenParking = value) }
            PropertyFormField.AVAILABLE_FROM -> updatePreferences(field) { it.copy(availableFrom = value) }
            else -> logWarning("Invalid field for updateFormValue: $field")
        }
    }

    fun updateFormValue(field: PropertyFormField, value: Boolean) {
        when (field) {
            PropertyFormField.IS_PET_FRIENDLY -> updatePreferences(field) { it.copy(isPetAllowed = value) }
            PropertyFormField.IS_MAINTENANCE_SEPARATE -> updatePricing(field) { it.copy(isMaintenanceSeparate = value) }
            else -> logWarning("Invalid field for updateFormValue: $field")
        }
    }

    // Update Functions For Basic UI
    fun updatePropertyType(value: PropertyType) = updatePropertyBasic(PropertyFormField.TYPE) {
        it.copy(type = value)
    }

    fun updateBHK(value: BHK) = updatePropertyBasic(PropertyFormField.BHK) {
        it.copy(bhk = value)
    }

    // Update Functions For PropertyPreferencesUI
    fun updateFurnishing(value: FurnishingType) = updatePreferences(PropertyFormField.FURNISHING_TYPE) {
        it.copy(furnishingType = value)
    }

    fun updatePreferredTenants(value: List<TenantType>) = updatePreferences(PropertyFormField.PREFERRED_TENANT_TYPE) {
        it.copy(preferredTenantTypes = value)
    }

    fun updatePreferredBachelor(value: BachelorType) = updatePreferences(PropertyFormField.PREFERRED_BACHELOR_TYPE) {
        it.copy(preferredBachelorType = value)
    }

    fun onInternalAmenityChanged(amenity: InternalAmenity, value: Boolean) {
        _internalAmenitySet.value = _internalAmenitySet.value!!.toMutableSet().apply {
            if (value)
                add(amenity)
            else
                remove(amenity)
        }
    }

    fun onSocialAmenityChanged(amenity: SocialAmenity, value: Boolean) {
        _socialAmenitySet.value = _socialAmenitySet.value!!.apply {
            if (value)
                this + amenity
            else
                this - amenity
        }
    }

    fun setPropertyImages(newUris: List<Uri>) {
        _imageUris.value = newUris
    }

    fun addPropertyImages(newUris: List<Uri>) {
        val currentUris = _imageUris.value!!.toMutableList()
        currentUris.addAll(newUris)
        _imageUris.value = currentUris
    }

    fun addPropertyImage(newUri: Uri) {
        val currentUris = _imageUris.value!!.toMutableList()
        currentUris.add(newUri)
        _imageUris.value = currentUris
    }

    fun removePropertyImage(imageUri: Uri) {
        val currentImages = _imageUris.value?.toMutableList() ?: run {
            logWarning("no images to remove")
            return
        }

        currentImages.remove(imageUri)
        _imageUris.value = currentImages
    }

    fun createProperty() {
        // Run validation
        if (!checkValidation()) {
            logDebug("Validation failed to continue create property, aborting...")
            _validationError.value = "Validation Failed"
            return
        }

        logDebug("Validation success")
        // Handle logic
        val property: Property? = builtPropertyDomain()
        if (property == null) {
            logError("Built Property failed")
            return
        }

        viewModelScope.launch {
            _createPropertyResult.value = ResultUI.Loading
            when (val result = propertyUseCase.createProperty(property)) {
                is Result.Success<Long> -> {
                    _createPropertyResult.value = ResultUI.Success(result.data)
                    logInfo("Property(${result.data}) Created Successfully")
                }
                is Result.Error -> {
                    _createPropertyResult.value = ResultUI.Error(result.message)
                    logError("Property Creation failed")
                }
            }
        }
    }

    fun resetForm() {
        // Reset Main Result
        _createPropertyResult.value = null

        // Reset Form Data
        _propertyBasicUI.value = PropertyBasicUI()
        _propertyPreferencesUI.value = PropertyPreferencesUI()
        _propertyAddressUI.value = PropertyAddressUI()
        _propertyPricingUI.value = PropertyPricingUI()
        _imageUris.value = emptyList()
        // Reset Amenities
        _icAmenityMap.value = emptyMap()
        _internalAmenitySet.value = emptySet()
        _socialAmenitySet.value = emptySet()

        // Reset Form Error
        formErrorMap.forEach { (key, _) -> formErrorMap.getValue(key).value = null }
        logInfo("Form reset is done.")
    }

    private fun checkValidation(): Boolean {
        val basicData = _propertyBasicUI.value!!
        val preferencesData = _propertyPreferencesUI.value!!
        val pricingData = _propertyPricingUI.value!!
        val addressData = _propertyAddressUI.value!!
        var isValidationSuccess = true

        // Helper function to reduce repetition
        fun updateError(field: PropertyFormField, errorMessage: String) {
            val errorField = formErrorMap.getValue(field)
            errorField.value = errorMessage
            isValidationSuccess = false
        }

        // Name Validation
        when {
            basicData.name.isEmpty() -> "enter valid input"
            basicData.name.length < 3 -> "length should be greater than 3"
            basicData.name.length > 100 -> "length should be less than 100"
            else -> null
        }?.let {
            updateError(PropertyFormField.NAME, it)
        }

        fun getFormData(field: PropertyFormField) = when (field) {
            PropertyFormField.TYPE -> basicData.type
            PropertyFormField.BHK -> basicData.bhk
            PropertyFormField.FURNISHING_TYPE -> preferencesData.furnishingType
            PropertyFormField.PREFERRED_TENANT_TYPE -> preferencesData.preferredTenantTypes
            PropertyFormField.NAME -> basicData.name
            PropertyFormField.DESCRIPTION -> basicData.description
            PropertyFormField.KIND -> basicData.kind
            PropertyFormField.PREFERRED_BACHELOR_TYPE -> preferencesData.preferredBachelorType
            PropertyFormField.COVERED_PARKING_COUNT -> preferencesData.countOfCoveredParking
            PropertyFormField.OPEN_PARKING_COUNT -> preferencesData.countOfOpenParking
            PropertyFormField.AVAILABLE_FROM -> preferencesData.availableFrom
            PropertyFormField.BUILT_UP_AREA -> basicData.builtUpArea
            PropertyFormField.BATH_ROOM_COUNT -> basicData.bathRoomCount
            PropertyFormField.IS_PET_FRIENDLY -> preferencesData.isPetAllowed
            PropertyFormField.PRICE -> pricingData.price
            PropertyFormField.IS_MAINTENANCE_SEPARATE -> pricingData.isMaintenanceSeparate
            PropertyFormField.MAINTENANCE_CHARGES -> pricingData.maintenanceCharges
            PropertyFormField.SECURITY_DEPOSIT -> pricingData.securityDepositAmount
            PropertyFormField.STREET -> addressData.street
            PropertyFormField.LOCALITY -> addressData.locality
            PropertyFormField.CITY -> addressData.city
            PropertyFormField.IMAGES -> TODO()
        }

        // Selectable enum types validation
        listOf(
            PropertyFormField.TYPE,
            PropertyFormField.FURNISHING_TYPE,
            PropertyFormField.BHK,
            PropertyFormField.PREFERRED_TENANT_TYPE,
            PropertyFormField.IS_MAINTENANCE_SEPARATE,
            PropertyFormField.IS_PET_FRIENDLY
        ).forEach { filed ->
            if (getFormData(filed) == null)
                updateError(filed, "select one")
        }

        // MAINTENANCE_CHARGES Validation
        if (pricingData.isMaintenanceSeparate == true && pricingData.maintenanceCharges.isEmpty())
            updateError(PropertyFormField.MAINTENANCE_CHARGES, "enter valid input")


        // Int Input Validations
        listOf(
            PropertyFormField.BUILT_UP_AREA,
            PropertyFormField.PRICE,
            PropertyFormField.SECURITY_DEPOSIT
        ).forEach {
            val value = getFormData(it)
            if (value !is String || value.toIntOrNull() == null)
                updateError(it, "enter valid input")
        }

        // String Input Validations
        listOf(
            PropertyFormField.AVAILABLE_FROM,
            PropertyFormField.STREET,
            PropertyFormField.LOCALITY,
            PropertyFormField.CITY,
        ).forEach {
            val value = getFormData(it) as? String
            if (value.isNullOrEmpty())
                updateError(it, "enter valid input")
        }

        return isValidationSuccess
    }

    private fun buildAmenities(): List<AmenityDomain> {
        val amenities = mutableListOf<AmenityDomain>()

        // Add Countable Internal Amenities
        icAmenityMap.value!!.forEach { (icAmenity, count) ->
            amenities.add(AmenityDomain(
                id = null,
                name = icAmenity,
                type = AmenityType.INTERNAL_COUNTABLE,
                count = count
            ))
        }

        // Add Internal Amenities
        internalAmenitySet.value!!.forEach { intAmenity ->
            amenities.add(AmenityDomain(
                id = null,
                name = intAmenity,
                type = AmenityType.INTERNAL,
            ))
        }

        // Add Internal Amenities
        socialAmenitySet.value!!.forEach { socialAmenity ->
            amenities.add(AmenityDomain(
                id = null,
                name = socialAmenity,
                type = AmenityType.SOCIAL,
            ))
        }

        return amenities
    }

    fun builtPropertyDomain() : Property? {
        try {
            val basicData = _propertyBasicUI.value!!
            val preferencesData = _propertyPreferencesUI.value!!
            val pricingData = _propertyPricingUI.value!!
            val addressData = _propertyAddressUI.value!!

            val address = PropertyAddress(
                streetName = addressData.street,
                locality = addressData.locality,
                city = addressData.city
            )

            val amenities = buildAmenities()

            val propertyImages = _imageUris.value?.map {
                PropertyImage(null, ImageSource.Uri(it), false)
            } ?: emptyList()

            return Property(
                id = null,
                landlordId = 1,
                name = basicData.name,
                description = basicData.description,
                lookingTo = basicData.lookingTo,
                kind = PropertyKind.RESIDENTIAL,
                type = basicData.type!!,
                furnishingType = preferencesData.furnishingType!!,
                amenities = amenities,
                preferredTenantType = preferencesData.preferredTenantTypes!!,
                preferredBachelorType = preferencesData.preferredBachelorType,
                transactionType = null,
                ageOfProperty = 0,
                countOfCoveredParking = preferencesData.countOfCoveredParking.toInt(),
                countOfOpenParking = preferencesData.countOfOpenParking.toInt(),
                availableFrom = preferencesData.availableFrom.toEpochSeconds(),
                bhk = basicData.bhk!!,
                builtUpArea = basicData.builtUpArea.toInt(),
                bathRoomCount = basicData.bathRoomCount.toInt(),
                isPetAllowed = preferencesData.isPetAllowed!!,
                isActive = true,
                price = pricingData.price.toInt(),
                isMaintenanceSeparate = pricingData.isMaintenanceSeparate!!,
                maintenanceCharges = pricingData.maintenanceCharges.toIntOrNull(),
                securityDepositAmount = pricingData.securityDepositAmount.toInt(),
                address = address,
                images = propertyImages,
                createdAt = System.currentTimeMillis()
            )
        } catch (exp: Exception) {
            logError(exp.message.toString(), exp)
            return null
        }
    }

    fun clearValidationError() {
        _validationError.value = null
    }
}


class CreatePropertyViewModelFactory(
    private val propertyUseCase: PropertyUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreatePropertyViewModel::class.java))
            return CreatePropertyViewModel(propertyUseCase) as T

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}