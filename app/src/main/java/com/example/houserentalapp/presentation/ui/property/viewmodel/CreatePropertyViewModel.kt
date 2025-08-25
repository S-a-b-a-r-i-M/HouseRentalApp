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
import com.example.houserentalapp.domain.model.enums.SocialAmenity
import com.example.houserentalapp.domain.model.enums.TenantType
import com.example.houserentalapp.domain.usecase.CreatePropertyUseCase
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.domain.utils.toEpoch
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logWarning
import kotlinx.coroutines.launch

class CreatePropertyViewModel(
    private val createPropertyUseCase: CreatePropertyUseCase
) : ViewModel() {
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    private val _success = MutableLiveData<String?>()
    val success: LiveData<String?> = _success
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

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

    private val imageUris = MutableLiveData<List<Uri>>(emptyList())

    fun getFormDataMap(field: PropertyFormField) : LiveData<String?> = formDataMap.getValue(field)

    fun getFormErrorMap(field: PropertyFormField) : LiveData<String?> = formErrorMap.getValue(field)

    fun updateFormValue(field: PropertyFormField, value: Any) {
        if (field == PropertyFormField.COVERED_PARKING_COUNT || field == PropertyFormField.OPEN_PARKING_COUNT) {
            val valueInt = value as? Int ?: run {
                logError("updateFormValue -> $field, given value is not a integer")
                return
            }
            updateParkingInternal(field, valueInt)
            return
        }

        val formFieldData = formDataMap.getValue(field)
        val formFieldErr = formErrorMap.getValue(field)
        formFieldData.value = value as String
        if (formFieldErr.value != null) formFieldErr.value = null
    }

    fun setPropertyImages(imageUris: List<Uri>) {
        this.imageUris.value = imageUris
    }

    fun removePropertyImage(imageUri: Uri) {
        val currentImages = imageUris.value?.toMutableList() ?: run {
            logWarning("no images to remove")
            return
        }

        currentImages.remove(imageUri)
        imageUris.value = currentImages
    }

    private fun updateParkingInternal(field: PropertyFormField, updateValue: Int) {
        formDataMap.getValue(field).apply {
            value = ((value?.toIntOrNull() ?: 0) + updateValue).toString()
        }
    }

    fun updateCoveredParking(updateValue: Int) {
        updateParkingInternal(PropertyFormField.COVERED_PARKING_COUNT, updateValue)
    }

    fun updateOpenParking(updateValue: Int) {
        updateParkingInternal(PropertyFormField.OPEN_PARKING_COUNT, updateValue)
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
            _loading.value = true
            when (val result = createPropertyUseCase(property)) {
                is Result.Success<Long> -> {
                    _loading.value = false
                    _success.value = "Property Created Successfully"
                }
                is Result.Error -> {
                    _loading.value = false
                    _error.value = "Property Creation failed"
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

        // LOOKING_TO Validation
        validateField(PropertyFormField.LOOKING_TO) { value ->
            when {
                value == null -> "select one"
                !LookingTo.isValid(value as String) -> {
                    logWarning("Value $value is not found in LookingTo")
                    "error"
                }
                else -> null
            }
        }

        // KIND Validation (skip)

        // TYPE Validation
        validateField(PropertyFormField.TYPE) { value ->
            when {
                value == null -> "select one"
                !PropertyType.isValid(value as String) -> {
                    logWarning("Value $value is not found in PropertyType")
                    "error"
                }
                else -> null
            }
        }

        // FURNISHING_TYPE Validation
        validateField(PropertyFormField.FURNISHING_TYPE) { value ->
            when {
                value == null -> "select one"
                !FurnishingType.isValid(value as String) -> {
                    logWarning("Value $value is not found in FurnishingType")
                    "error"
                }
                else -> null
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

        // BHK Validation
        validateField(PropertyFormField.BHK) { value ->
            when {
                value == null -> "select one"
                !BHK.isValid(value as String) -> {
                    logWarning("Value $value is not found in BHK")
                    "error"
                }
                else -> null
            }
        }

        // BUILT_UP_AREA Validation
        validateField(PropertyFormField.BUILT_UP_AREA) { value ->
            if (value == null) "enter valid input" else null
        }

        // IS_PET_ALLOWED Validation
        validateField(PropertyFormField.IS_PET_FRIENDLY) { value ->
            if (value == null) "enter valid input" else null
        }

        // AVAILABLE_FROM Validation
        validateField(PropertyFormField.AVAILABLE_FROM) { value ->
            if (value == null || value == "") "enter valid input" else null
        }

        // PRICE Validation
        validateField(PropertyFormField.PRICE) { value ->
            if (value == null) "enter valid input" else null
        }

        // IS_MAINTENANCE_SEPARATE Validation
        val isMaintenanceSeparateValue = validateField(PropertyFormField.IS_MAINTENANCE_SEPARATE) { value ->
            if (value == null) "select one" else null
        } as? Boolean

        // MAINTENANCE_CHARGES Validation
        validateField(PropertyFormField.MAINTENANCE_CHARGES) { value ->
            if (isMaintenanceSeparateValue == true && value == null) "enter valid input" else null
        }

        // SECURITY_DEPOSIT Validation
        validateField(PropertyFormField.SECURITY_DEPOSIT) { value ->
            if (value == null) "enter valid input" else null
        }

        // Address Validations
//        validateField(NewPropertyFormField.STREET_NAME) { value ->
//            if (value == null || (value as? String)?.isEmpty() == true) "enter valid input" else null
//        }

        validateField(PropertyFormField.LOCALITY) { value ->
            if (value == null || (value as? String)?.isEmpty() == true) "enter valid input" else null
        }

        validateField(PropertyFormField.CITY) { value ->
            if (value == null || (value as? String)?.isEmpty() == true) "enter valid input" else null
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
                amenity = icAmenity.readable,
                amenityType = AmenityType.INTERNAL_COUNTABLE,
                count = count.value
            ))
        }

        // Add Internal Amenities
        internalAmenityMap.filter {
            (_, isSelected) -> isSelected.value == true
        }.forEach { (intAmenity) ->
            amenities.add(Amenity(
                id = null,
                amenity = intAmenity.readable,
                amenityType = AmenityType.INTERNAL,
            ))
        }

        // Add Internal Amenities
        socialAmenityMap.filter {
            (_, count) -> count.value == true
        }.forEach { (socialAmenity) ->
            amenities.add(Amenity(
                id = null,
                amenity = socialAmenity.readable,
                amenityType = AmenityType.SOCIAL,
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
                    streetName = getValue(PropertyFormField.STREET_NAME).value ?: "",
                    locality = getValue(PropertyFormField.LOCALITY).value!!,
                    city = getValue(PropertyFormField.CITY).value!!
                )
                val propertyImages = imageUris.value?.map {
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
                    bathRoomCount = 0,
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

    /**
     * Clear error message (usually called after showing error to user)
     */
    fun clearErr() {
        _error.value = null
    }

    fun clearFieldErr(field: PropertyFormField) {
        formErrorMap[field]?.value = null
    }

    /**
     * Clear success message (usually called after showing success to user)
     */
    fun clearSuccess() {
        _success.value = null
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