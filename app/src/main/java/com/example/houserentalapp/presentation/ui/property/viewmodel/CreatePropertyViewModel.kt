package com.example.houserentalapp.presentation.ui.property.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.houserentalapp.domain.model.Amenity
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
import com.example.houserentalapp.domain.model.enums.ReadableEnum
import com.example.houserentalapp.domain.model.enums.SocialAmenity
import com.example.houserentalapp.domain.model.enums.TenantType
import com.example.houserentalapp.domain.usecase.CreatePropertyUseCase
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.domain.utils.toEpoch
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

    private val internalCountableAmenityMap = CountableInternalAmenity.entries.associateWith {
        MutableLiveData<Int>(0)
    }

    private val internalAmenityMap = InternalAmenity.entries.associateWith {
        MutableLiveData<Boolean>(false)
    }

    private val socialAmenityMap = SocialAmenity.entries.associateWith {
        MutableLiveData<Boolean>(false)
    }

    private val _imageUris = MutableLiveData<List<Uri>>(emptyList())
    val imageUris: LiveData<List<Uri>> = _imageUris

    fun getFormDataMap(field: PropertyFormField) : LiveData<String?> = formDataMap.getValue(field)

    fun getFormErrorMap(field: PropertyFormField) : LiveData<String?> = formErrorMap.getValue(field)

    fun updateFormValue(field: PropertyFormField, value: Any) {
        if (field == PropertyFormField.COVERED_PARKING_COUNT ||
            field == PropertyFormField.OPEN_PARKING_COUNT ||
            field == PropertyFormField.BATH_ROOM_COUNT
        ) {
            val valueInt = value as? Int ?: run {
                logError("updateFormValue -> $field, given value is not a integer")
                return
            }
            updateCounterValue(field, valueInt)
            return
        }

        val formFieldData = formDataMap.getValue(field)
        val formFieldErr = formErrorMap.getValue(field)
        formFieldData.value = value as String
        if (formFieldErr.value != null) formFieldErr.value = null
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

    private fun updateCounterValue(field: PropertyFormField, updateValue: Int) {
        formDataMap.getValue(field).apply {
            value = ((value?.toIntOrNull() ?: 0) + updateValue).toString()
        }
    }

    fun getInternalCountableAmenity(amenity: CountableInternalAmenity): LiveData<Int> =
        internalCountableAmenityMap.getValue(amenity)

    fun updateInternalCountableAmenity(amenity: CountableInternalAmenity, updateValue: Int) {
        internalCountableAmenityMap.getValue(amenity).apply {
            value = (value ?: 0) + updateValue
        }
    }

    fun getInternalAmenity(amenity: InternalAmenity): LiveData<Boolean> =
        internalAmenityMap.getValue(amenity)

    fun onInternalAmenityChanged(amenity: InternalAmenity, value: Boolean) {
        internalAmenityMap.getValue(amenity).value = value
    }

    fun getSocialAmenity(amenity: SocialAmenity): LiveData<Boolean> =
        socialAmenityMap.getValue(amenity)

    fun onSocialAmenityChanged(amenity: SocialAmenity, value: Boolean) {
        socialAmenityMap.getValue(amenity).value = value
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
        val isMaintenanceSeparateValue = validateField(PropertyFormField.IS_MAINTENANCE_SEPARATE) { value ->
            if (value == null) "select one" else null
        } as? Boolean

        // MAINTENANCE_CHARGES Validation
        validateField(PropertyFormField.MAINTENANCE_CHARGES) { value ->
            if (isMaintenanceSeparateValue == true && value == null) "enter valid input" else null
        }

        // Int Input Validations
        listOf(PropertyFormField.BUILT_UP_AREA, PropertyFormField.PRICE, PropertyFormField.SECURITY_DEPOSIT).forEach {
            validateField(it) { value ->
                if (value == null || value as? Int == null) "enter valid input" else null
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
                    if (value == null || (value as? String)?.isEmpty() == true) "enter valid input" else null
                }
            }

        return isValidationSuccess
    }

    private fun buildAmenities(): List<Amenity> {
        val amenities = mutableListOf<Amenity>()

        // Add Countable Internal Amenities
        internalCountableAmenityMap.filter {
            (_, count) -> count.value!! > 0
        }.forEach { (icAmenity, count) ->
            amenities.add(Amenity(
                id = null,
                name = icAmenity.readable,
                type = AmenityType.INTERNAL_COUNTABLE,
                count = count.value
            ))
        }

        // Add Internal Amenities
        internalAmenityMap.filter {
            (_, isSelected) -> isSelected.value == true
        }.forEach { (intAmenity) ->
            amenities.add(Amenity(
                id = null,
                name = intAmenity.readable,
                type = AmenityType.INTERNAL,
            ))
        }

        // Add Internal Amenities
        socialAmenityMap.filter {
            (_, count) -> count.value == true
        }.forEach { (socialAmenity) ->
            amenities.add(Amenity(
                id = null,
                name = socialAmenity.readable,
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
                    streetName = getValue(PropertyFormField.STREET).value ?: "",
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
                    availableFrom = getValue(PropertyFormField.AVAILABLE_FROM).value!!.toEpoch(),
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