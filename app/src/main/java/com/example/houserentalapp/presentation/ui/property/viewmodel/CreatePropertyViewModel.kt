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
import com.example.houserentalapp.domain.model.enums.CountableInternalAmenity
import com.example.houserentalapp.domain.model.enums.FurnishingType
import com.example.houserentalapp.domain.model.enums.InternalAmenity
import com.example.houserentalapp.domain.model.enums.LookingTo
import com.example.houserentalapp.domain.model.enums.PropertyKind
import com.example.houserentalapp.domain.model.enums.PropertyType
import com.example.houserentalapp.domain.model.enums.SocialAmenity
import com.example.houserentalapp.domain.model.enums.TenantType
import com.example.houserentalapp.domain.usecase.CreatePropertyUseCase
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.utils.helpers.toEpochSeconds
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.logWarning
import com.example.houserentalapp.presentation.utils.extensions.simpleClassName
import kotlinx.coroutines.launch

class CreatePropertyViewModel(
    private val createPropertyUseCase: CreatePropertyUseCase
) : ViewModel() {
    private val _createPropertyResult = MutableLiveData<ResultUI<Long>>()
    val createPropertyResult: LiveData<ResultUI<Long>> = _createPropertyResult

    private val formDataMap = PropertyFormField.entries.associateWith {
        MutableLiveData<String?>(null)
    }

    private val formErrorMap = PropertyFormField.entries.associateWith {
        MutableLiveData<String?>(null)
    }

    private val _icAmenityMap = MutableLiveData<Map<CountableInternalAmenity, Int>>(emptyMap())
    val icAmenityMap: LiveData<Map<CountableInternalAmenity, Int>> = _icAmenityMap
    private val _internalAmenityMap = MutableLiveData<Set<InternalAmenity>>(emptySet())
    val internalAmenityMap: LiveData<Set<InternalAmenity>> = _internalAmenityMap
    private val _socialAmenityMap = MutableLiveData<Set<SocialAmenity>>(emptySet())
    val socialAmenityMap: LiveData<Set<SocialAmenity>> = _socialAmenityMap

    private val _imageUris = MutableLiveData<List<Uri>>(emptyList())
    val imageUris: LiveData<List<Uri>> = _imageUris

    fun getFormDataMap(field: PropertyFormField) : LiveData<String?> = formDataMap.getValue(field)

    fun getFormErrorMap(field: PropertyFormField) : LiveData<String?> = formErrorMap.getValue(field)

    fun updateFormValue(field: PropertyFormField, value: Any) {
        val formFieldData = formDataMap.getValue(field)
        val formFieldErr = formErrorMap.getValue(field)
        formFieldData.value = value as String
        if (formFieldErr.value != null) formFieldErr.value = null
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

    fun onInternalAmenityChanged(amenity: InternalAmenity, value: Boolean) {
        _internalAmenityMap.value = _internalAmenityMap.value!!.toMutableSet().apply {
            if (value)
                add(amenity)
            else
                remove(amenity)
        }
    }

    fun onSocialAmenityChanged(amenity: SocialAmenity, value: Boolean) {
        _socialAmenityMap.value = _socialAmenityMap.value!!.apply {
            if (value)
                this + amenity
            else
                this - amenity
        }
    }

    fun setPropertyImages(imageUris: List<Uri>) {
        this._imageUris.value = imageUris
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
            when (val result = createPropertyUseCase(property)) {
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

    }

    private fun checkValidation(): Boolean {
        var isValidationSuccess = true

        // Helper function to reduce repetition
        fun validateField(field: PropertyFormField, validator: (Any?) -> String?): Any? {
            val value = formDataMap.getValue(field).value
            val errorField = formErrorMap.getValue(field)
            val errorMessage = validator(value)

            if (errorMessage != null) {
                errorField.value = errorMessage
                isValidationSuccess = false
            }
            return value
        }

        // Name Validation
        validateField(PropertyFormField.NAME) { value ->
            when {
                value == null || (value as? String)?.isEmpty() == true -> "enter valid input"
                (value as String).length < 3 -> "length should be greater than 3"
                value.length > 100 -> "length should be less than 100"
                else -> null
            }
        }

        // LOOKING_TO, TYPE, FURNISHING_TYPE, BHK Validation
        listOf(
            Pair(PropertyFormField.LOOKING_TO, LookingTo),
            Pair(PropertyFormField.TYPE, PropertyType),
            Pair(PropertyFormField.FURNISHING_TYPE, FurnishingType),
            Pair(PropertyFormField.BHK, BHK)
        ).forEach { (filed, enum) ->
            validateField(filed) { value ->
                when {
                    value == null -> "select one"
                    !enum.isValid(value as String) -> {
                        logWarning("Value $value is not found in ${enum.simpleClassName}")
                        "error"
                    }
                    else -> null
                }
            }
        }

        // PREFERRED_TENANT_TYPE Validation
        val tenantValue = validateField(PropertyFormField.PREFERRED_TENANT_TYPE) { value ->
            when (value){
                null, "" -> "select one"
                else -> null
            }
        } as? String

        // PREFERRED_BACHELOR_TYPE Validation (conditional)
        if (tenantValue?.contains(TenantType.BACHELORS.readable) == true)
            validateField(PropertyFormField.PREFERRED_BACHELOR_TYPE) { value ->
                when (value){
                    null, "" -> "select one"
                    else -> null
                }
            }

        // IS_MAINTENANCE_SEPARATE Validation
        val maintenanceSeparate = validateField(PropertyFormField.IS_MAINTENANCE_SEPARATE) { value ->
            if (value == null) "select one" else null
        } as String

        // MAINTENANCE_CHARGES Validation
        validateField(PropertyFormField.MAINTENANCE_CHARGES) { value ->
            if (maintenanceSeparate.lowercase() == "separate" &&
                (value !is String || value.toIntOrNull() == null))
                "enter valid input"
            else
                null
        }

        // Int Input Validations
        listOf(PropertyFormField.BUILT_UP_AREA, PropertyFormField.PRICE, PropertyFormField.SECURITY_DEPOSIT).forEach {
            validateField(it) { value ->
                if (value !is String || value.toIntOrNull() == null) "enter valid input" else null
            }
        }

        // String Input Validations
        listOf(
            PropertyFormField.AVAILABLE_FROM,
            PropertyFormField.STREET,
            PropertyFormField.LOCALITY,
            PropertyFormField.CITY,
            PropertyFormField.IS_PET_FRIENDLY
        ).forEach {
                validateField(it) { value ->
                    if (value !is String || value.isBlank()) "enter valid input" else null
                }
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
        internalAmenityMap.value!!.forEach { intAmenity ->
            amenities.add(AmenityDomain(
                id = null,
                name = intAmenity,
                type = AmenityType.INTERNAL,
            ))
        }

        // Add Internal Amenities
        socialAmenityMap.value!!.forEach { socialAmenity ->
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
            with(formDataMap) {
                val tenantTypes = getValue(PropertyFormField.PREFERRED_TENANT_TYPE).value!!
                    .split(",")
                    .map { TenantType.fromString(it)!! }
                val bachelorType = if (TenantType.BACHELORS in tenantTypes)
                    getValue(PropertyFormField.PREFERRED_BACHELOR_TYPE).value!!
                else
                    null

                val isMaintenanceSeparate = getValue(
                    PropertyFormField.IS_MAINTENANCE_SEPARATE
                ).value?.lowercase() == "separate"
                val maintenanceCharges = if (isMaintenanceSeparate) getValue(
                    PropertyFormField.MAINTENANCE_CHARGES
                ).value!!.toInt() else 0

                val address = PropertyAddress(
                    streetName = getValue(PropertyFormField.STREET).value!!,
                    locality = getValue(PropertyFormField.LOCALITY).value!!,
                    city = getValue(PropertyFormField.CITY).value!!
                )

                val propertyImages = _imageUris.value?.map {
                    PropertyImage(null, "", ImageSource.Uri(it), false)
                } ?: emptyList()

                val amenities = buildAmenities()

                return Property(
                    id = null,
                    landlordId = 1,
                    name = getValue(PropertyFormField.NAME).value!!,
                    description = getValue(PropertyFormField.DESCRIPTION).value,
                    lookingTo = LookingTo.fromString(getValue(PropertyFormField.LOOKING_TO).value!!)!!,
                    kind = PropertyKind.COMMERCIAL,
                    type = PropertyType.fromString(getValue(PropertyFormField.TYPE).value!!)!!,
                    furnishingType = FurnishingType.fromString(getValue(PropertyFormField.FURNISHING_TYPE).value!!)!!,
                    amenities = amenities,
                    preferredTenantType = tenantTypes,
                    preferredBachelorType = bachelorType,
                    transactionType = null,
                    ageOfProperty = 0,
                    countOfCoveredParking = getValue(PropertyFormField.COVERED_PARKING_COUNT).value?.toInt() ?: 0,
                    countOfOpenParking = getValue(PropertyFormField.OPEN_PARKING_COUNT).value?.toInt() ?: 0,
                    availableFrom = getValue(PropertyFormField.AVAILABLE_FROM).value!!.toEpochSeconds(),
                    bhk = BHK.fromString(getValue(PropertyFormField.BHK).value!!)!!,
                    builtUpArea = getValue(PropertyFormField.BUILT_UP_AREA).value?.toInt() ?: 0,
                    bathRoomCount = getValue(PropertyFormField.BATH_ROOM_COUNT).value?.toInt() ?: 0,
                    isPetAllowed = getValue(PropertyFormField.IS_PET_FRIENDLY).value?.toBoolean() ?: false,
                    isAvailable = true,
                    price = getValue(PropertyFormField.BUILT_UP_AREA).value!!.toInt(),
                    isMaintenanceSeparate = isMaintenanceSeparate,
                    maintenanceCharges = maintenanceCharges,
                    securityDepositAmount = getValue(PropertyFormField.SECURITY_DEPOSIT).value!!.toInt(),
                    address = address,
                    images = propertyImages,
                    createdAt = System.currentTimeMillis()
                )
            }
        } catch (exp: Exception) {
            logError(exp.message.toString(), exp)
            return null
        }
    }
}


class CreatePropertyViewModelFactory(
    private val createPropertyUseCase: CreatePropertyUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreatePropertyViewModel::class.java))
            return CreatePropertyViewModel(createPropertyUseCase) as T

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}