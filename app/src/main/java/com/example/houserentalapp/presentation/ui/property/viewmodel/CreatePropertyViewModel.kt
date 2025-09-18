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
import com.example.houserentalapp.domain.model.enums.PropertyFields
import com.example.houserentalapp.domain.model.enums.PropertyKind
import com.example.houserentalapp.domain.model.enums.PropertyType
import com.example.houserentalapp.domain.model.enums.ReadableEnum
import com.example.houserentalapp.domain.model.enums.SocialAmenity
import com.example.houserentalapp.domain.model.enums.TenantType
import com.example.houserentalapp.domain.usecase.PropertyUseCase
import com.example.houserentalapp.domain.utils.Result
import com.example.houserentalapp.presentation.enums.PropertyFormField
import com.example.houserentalapp.presentation.model.PropertyDataUI
import com.example.houserentalapp.presentation.utils.helpers.toEpochSeconds
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.logWarning
import com.example.houserentalapp.presentation.utils.helpers.fromEpoch
import kotlinx.coroutines.launch
import kotlin.String
import kotlin.collections.emptyMap

class CreatePropertyViewModel(private val propertyUseCase: PropertyUseCase) : ViewModel() {

    private object InitialData {
        var basic = PropertyDataUI()
        var images = emptyList<PropertyImage>()
        var icAmenity = emptyMap<CountableInternalAmenity, AmenityDomain>()
        var internalAmenity = emptyMap<InternalAmenity, AmenityDomain>()
        var socialAmenity = emptyMap<SocialAmenity, AmenityDomain>()

        fun reset() {
            basic = PropertyDataUI()
            images = emptyList()
            icAmenity = emptyMap()
            internalAmenity = emptyMap()
            socialAmenity = emptyMap()
        }
    }

    private val _submitPropertyResult = MutableLiveData<ResultUI<Boolean>?>()
    val submitPropertyResult: LiveData<ResultUI<Boolean>?> = _submitPropertyResult

    // Images
    private val _images = MutableLiveData(InitialData.images)
    val images: LiveData<List<PropertyImage>> = _images

    // Amenities
    private val _icAmenityMap = MutableLiveData(InitialData.icAmenity)
    val icAmenityMap: LiveData<Map<CountableInternalAmenity, AmenityDomain>> = _icAmenityMap
    private val _internalAmenityMap = MutableLiveData(InitialData.internalAmenity)
    val internalAmenityMap: LiveData<Map<InternalAmenity, AmenityDomain>> = _internalAmenityMap
    private val _socialAmenityMap = MutableLiveData(InitialData.socialAmenity)
    val socialAmenityMap: LiveData<Map<SocialAmenity, AmenityDomain>> = _socialAmenityMap

    // Errors
    private val _validationError = MutableLiveData<String?>()
    val validationError: LiveData<String?> = _validationError

    private val formErrorMap = PropertyFormField.entries.filter { it.isRequired }.associateWith {
        MutableLiveData<String?>(null)
    }

    // Property Data
    private var propertyDataUI = PropertyDataUI()
    private var propertyDomainToEdit: Property? = null

    fun getFormErrorMap(field: PropertyFormField) : LiveData<String?> = formErrorMap.getValue(field)

    fun clearFormFieldError(field: PropertyFormField) {
        val formFieldErr = formErrorMap[field] ?: run {
            logWarning("updateFormFieldError $field is not available in map")
            return
        }
        if (formFieldErr.value != null) formFieldErr.value = null
    }

    fun updateFormValue(field: PropertyFormField, value: String) {
        when (field) {
            PropertyFormField.NAME -> propertyDataUI.name = value
            PropertyFormField.DESCRIPTION -> propertyDataUI.description = value
            PropertyFormField.STREET -> propertyDataUI.street = value
            PropertyFormField.LOCALITY -> propertyDataUI.locality = value
            PropertyFormField.CITY -> propertyDataUI.city = value
            PropertyFormField.PRICE -> propertyDataUI.price = value
            PropertyFormField.MAINTENANCE_CHARGES -> propertyDataUI.maintenanceCharges = value
            PropertyFormField.BUILT_UP_AREA -> propertyDataUI.builtUpArea = value
            PropertyFormField.SECURITY_DEPOSIT -> propertyDataUI.securityDepositAmount = value
            PropertyFormField.BATH_ROOM_COUNT -> propertyDataUI.bathRoomCount = value
            PropertyFormField.COVERED_PARKING_COUNT -> propertyDataUI.countOfCoveredParking = value
            PropertyFormField.OPEN_PARKING_COUNT -> propertyDataUI.countOfOpenParking = value
            PropertyFormField.AVAILABLE_FROM -> propertyDataUI.availableFrom = value
            else -> logWarning("Invalid field for updateFormValue String: $field")
        }
    }

    fun updateFormValue(field: PropertyFormField, value: Boolean) {
        when (field) {
            PropertyFormField.IS_PET_FRIENDLY -> propertyDataUI.isPetAllowed = value
            PropertyFormField.IS_MAINTENANCE_SEPARATE -> propertyDataUI.isMaintenanceSeparate = value
            else -> {
                logWarning("Invalid field for updateFormValue Boolean: $field")
                return
            }
        }
        clearFormFieldError(field)
    }

    fun updateFormValue(field: PropertyFormField, value: ReadableEnum?) {
        when (field) {
            PropertyFormField.TYPE -> {
                if (value is PropertyType)
                    propertyDataUI.type = value
                else
                    logWarning("Invalid value for property type $value")
            }
            PropertyFormField.BHK -> {
                if (value is BHK)
                    propertyDataUI.bhk = value
                else
                    logWarning("Invalid value for property BHK $value")
            }
            PropertyFormField.FURNISHING_TYPE -> {
                if (value is FurnishingType)
                    propertyDataUI.furnishingType = value
                else
                    logWarning("Invalid value for furnishing type $value")
            }
            PropertyFormField.PREFERRED_BACHELOR_TYPE -> {
                if (value == null || value is BachelorType)
                    propertyDataUI.preferredBachelorType = value
                else
                    logWarning("Invalid value for property type $value")
            }
            else -> {
                logWarning("Invalid field for updateForValue mEnum: $field")
                return
            }
        }
        clearFormFieldError(field)
    }

    fun updatePreferredTenants(value: List<TenantType>) {
        propertyDataUI.preferredTenantTypes = value
        clearFormFieldError(PropertyFormField.PREFERRED_TENANT_TYPE)
    }

    fun updateInternalCountableAmenity(amenity: CountableInternalAmenity, updateValue: Int) {
        val mutableMap = _icAmenityMap.value!!.toMutableMap()
        val amenityDomain = mutableMap.getOrDefault(
        amenity,
        AmenityDomain(
            0,
            amenity,
            AmenityType.INTERNAL_COUNTABLE,
            0
        ))

        val newCount = (amenityDomain.count ?: 0) + updateValue
        if (newCount == 0)
            mutableMap.remove(amenity)
        else
            mutableMap.put(amenity, amenityDomain.copy(count = newCount))

        _icAmenityMap.value = mutableMap
    }

    fun onInternalAmenityChanged(amenity: InternalAmenity, value: Boolean) {
        val currentMap = _internalAmenityMap.value!!
        if (value && currentMap.containsKey(amenity)) return // Don't add exists value

        _internalAmenityMap.value = currentMap.toMutableMap().apply {
            if (value)
                put(amenity, AmenityDomain(0L, amenity, AmenityType.INTERNAL))
            else
                remove(amenity)
        }
    }

    fun onSocialAmenityChanged(amenity: SocialAmenity, value: Boolean) {
        val currentMap = _socialAmenityMap.value!!
        if (value && currentMap.containsKey(amenity)) return // Don't add exists value

        _socialAmenityMap.value = currentMap.toMutableMap().apply {
            if (value)
                put(amenity, AmenityDomain(0L, amenity, AmenityType.SOCIAL))
            else
                remove(amenity)
        }
    }

    fun addPropertyImage(newUri: Uri) {
        val currentUris = _images.value!!.toMutableList()
        currentUris.add(
            PropertyImage(0, ImageSource.Uri(newUri), isPrimary = currentUris.isEmpty())
        )
        _images.value = currentUris
        clearFormFieldError(PropertyFormField.IMAGES)
    }

    fun addPropertyImages(newUris: List<Uri>) {
        val currentImages = _images.value!!.toMutableList()
        currentImages.addAll(newUris.map {
            PropertyImage(0, ImageSource.Uri(it), isPrimary = false)
        })
        _images.value = currentImages
        clearFormFieldError(PropertyFormField.IMAGES)
    }

    fun removePropertyImage(propertyImage: PropertyImage) {
        val currentImages = _images.value?.toMutableList() ?: run {
            logWarning("no images to remove")
            return
        }

        currentImages.remove(propertyImage)
        _images.value = currentImages
    }

    fun createProperty(currentUserId: Long) {
        // Run validation
        if (!checkValidation()) {
            logDebug("Validation failed, aborting...")
            _validationError.value = "Validation Failed"
            return
        }
        logDebug("Validation success")

        // Handle logic
        val property: Property? = builtPropertyDomain(currentUserId)
        if (property == null) {
            logError("Built Property failed")
            return
        }

        viewModelScope.launch {
            _submitPropertyResult.value = ResultUI.Loading
            when (val result = propertyUseCase.createProperty(property)) {
                is Result.Success<Long> -> {
                    _submitPropertyResult.value = ResultUI.Success(true)
                    logInfo("Property(${result.data}) Created Successfully")
                }
                is Result.Error -> {
                    _submitPropertyResult.value = ResultUI.Error(result.message)
                    logError("Property Creation failed")
                }
            }
        }
    }

    private fun getModifiedFields(
        oldProperty: Property, modifiedProperty: Property
    ): List<PropertyFields> {
        val updatedFields = mutableListOf<PropertyFields>()

        // Basic property fields
        if (oldProperty.name != modifiedProperty.name) updatedFields.add(PropertyFields.NAME)

        if (oldProperty.description != modifiedProperty.description)
            updatedFields.add(PropertyFields.DESCRIPTION)

        if (oldProperty.kind != modifiedProperty.kind) updatedFields.add(PropertyFields.KIND)

        if (oldProperty.type != modifiedProperty.type) updatedFields.add(PropertyFields.TYPE)

        if (oldProperty.furnishingType != modifiedProperty.furnishingType)
            updatedFields.add(PropertyFields.FURNISHING_TYPE)

        // List comparison for preferred tenant types
        if (oldProperty.preferredTenantType != modifiedProperty.preferredTenantType)
            updatedFields.add(PropertyFields.PREFERRED_TENANT_TYPE)

        if (oldProperty.preferredBachelorType != modifiedProperty.preferredBachelorType)
            updatedFields.add(PropertyFields.PREFERRED_BACHELOR_TYPE)

        // Numeric fields
        if (oldProperty.bathRoomCount != modifiedProperty.bathRoomCount)
            updatedFields.add(PropertyFields.BATH_ROOM_COUNT)

        if (oldProperty.countOfCoveredParking != modifiedProperty.countOfCoveredParking)
            updatedFields.add(PropertyFields.COVERED_PARKING_COUNT)

        if (oldProperty.countOfOpenParking != modifiedProperty.countOfOpenParking)
            updatedFields.add(PropertyFields.OPEN_PARKING_COUNT)

        if (oldProperty.availableFrom != modifiedProperty.availableFrom)
            updatedFields.add(PropertyFields.AVAILABLE_FROM)

        if (oldProperty.bhk != modifiedProperty.bhk) updatedFields.add(PropertyFields.BHK)

        if (oldProperty.builtUpArea != modifiedProperty.builtUpArea)
            updatedFields.add(PropertyFields.BUILT_UP_AREA)

        // Boolean fields
        if (oldProperty.isPetAllowed != modifiedProperty.isPetAllowed)
            updatedFields.add(PropertyFields.IS_PET_FRIENDLY)

        // Pricing fields
        if (oldProperty.price != modifiedProperty.price) updatedFields.add(PropertyFields.PRICE)

        if (oldProperty.isMaintenanceSeparate != modifiedProperty.isMaintenanceSeparate)
            updatedFields.add(PropertyFields.IS_MAINTENANCE_SEPARATE)

        if (oldProperty.maintenanceCharges != modifiedProperty.maintenanceCharges)
            updatedFields.add(PropertyFields.MAINTENANCE_CHARGES)

        if (oldProperty.securityDepositAmount != modifiedProperty.securityDepositAmount)
            updatedFields.add(PropertyFields.SECURITY_DEPOSIT)

        // Address comparison
        if (oldProperty.address != modifiedProperty.address) {
            val addressChanges = getModifiedAddressFields(
                oldProperty.address, modifiedProperty.address
            )
            updatedFields.addAll(addressChanges)
        }

        // Amenities comparison
        if (oldProperty.amenities.toSet() != modifiedProperty.amenities.toSet()) // Converting to set because it wont compare insertion order
            updatedFields.add(PropertyFields.AMENITIES)

        // Images comparison
        if (oldProperty.images != modifiedProperty.images)
            updatedFields.add(PropertyFields.IMAGES)

        return updatedFields
    }

    // Helper function for address field comparison
    private fun getModifiedAddressFields(oldAddress: PropertyAddress, newAddress: PropertyAddress) =
        mutableListOf<PropertyFields>().apply {
            if (oldAddress.city != newAddress.city) add(PropertyFields.CITY)

            if (oldAddress.street != newAddress.street) add(PropertyFields.STREET)

            if (oldAddress.locality != newAddress.locality) add(PropertyFields.LOCALITY)
        }

    fun updateProperty(currentUserId: Long) {
        // Run validation
        if (!checkValidation()) {
            logDebug("Validation failed, aborting...")
            _validationError.value = "Validation Failed"
            return
        }
        logDebug("Validation success")

        val oldProperty = propertyDomainToEdit
        if (oldProperty == null) {
            logError("propertyToEdit Should not be null for update property")
            return
        }

        // Handle logic
        val property: Property? = builtPropertyDomain(currentUserId, oldProperty.id)
        if (property == null) {
            logError("Built Property failed")
            return
        }

        viewModelScope.launch {
            _submitPropertyResult.value = ResultUI.Loading
            // Get Modified Fields
            val updatedFields = getModifiedFields(oldProperty, property)
            when (val result = propertyUseCase.updateProperty(property, updatedFields)) {
                is Result.Success<*> -> {
                    _submitPropertyResult.value = ResultUI.Success(true)
                    logInfo("Property(${result.data}) updated successfully")
                }
                is Result.Error -> {
                    _submitPropertyResult.value = ResultUI.Error(result.message)
                    logError("Property update failed")
                }
            }
        }
    }

    private val _resultState = MutableLiveData<ResultUI<Boolean>>()
    fun loadPropertyToEdit(propertyId: Long, onSuccess: (PropertyDataUI) -> Unit) {
        _resultState.value = ResultUI.Loading
        viewModelScope.launch {
            try {
                when (val res = propertyUseCase.getProperty(propertyId)) {
                    is Result.Success<Property> -> {
                        logInfo("successfully loaded property(id: $propertyId)")

                        propertyDomainToEdit = res.data
                        val propertyUI = parsePropertyDataUI(res.data)
                        bindAmenitiesUI(res.data.amenities)
                        bindImages(res.data.images)

                        _resultState.value = ResultUI.Success(true)
                        onSuccess(propertyUI)
                    }
                    is Result.Error -> {
                        _resultState.value = ResultUI.Error(res.message)
                        logError("Error on loadProperty : ${res.message}")
                    }
                }
            } catch (exp: Exception) {
                _resultState.value = ResultUI.Error("Unexpected Error")
                logError("Error on loadProperty : ${exp.message}")
            }
        }
    }

    private fun parsePropertyDataUI(property: Property): PropertyDataUI {
        val dataUI = PropertyDataUI(
            name = property.name,
            description = property.description ?: "",
            lookingTo = property.lookingTo,
            kind = property.kind,
            type = property.type,
            bhk = property.bhk,
            builtUpArea = property.builtUpArea.toString(),
            bathRoomCount = property.bathRoomCount.toString(),
            price = property.price.toString(),
            isMaintenanceSeparate = property.isMaintenanceSeparate,
            maintenanceCharges = property.maintenanceCharges?.toString() ?: "",
            securityDepositAmount = property.securityDepositAmount.toString(),
            furnishingType = property.furnishingType,
            preferredTenantTypes = property.preferredTenantType,
            preferredBachelorType = property.preferredBachelorType,
            isPetAllowed = property.isPetAllowed,
            countOfCoveredParking = property.countOfCoveredParking.toString(),
            countOfOpenParking = property.countOfOpenParking.toString(),
            availableFrom = property.availableFrom.fromEpoch(),
            street = property.address.street,
            locality = property.address.locality,
            city = property.address.city,
        )
        InitialData.basic = dataUI.copy()
        propertyDataUI = dataUI
        return dataUI
    }

    private fun bindAmenitiesUI(amenities: List<AmenityDomain>) {
        if (amenities.isEmpty()) return

        val icAmenities = mutableMapOf<CountableInternalAmenity, AmenityDomain>()
        val internalAmenities = mutableMapOf<InternalAmenity, AmenityDomain>()
        val societyAmenities = mutableMapOf<SocialAmenity, AmenityDomain>()

        amenities.forEach {
            when(it.type) {
                AmenityType.INTERNAL -> {
                    internalAmenities[it.name as InternalAmenity] = it
                }
                AmenityType.SOCIAL -> {
                    societyAmenities[it.name as SocialAmenity] = it
                }
                AmenityType.INTERNAL_COUNTABLE -> {
                    it.count ?: run {
                        logWarning("Countable Internal Amenity(${it.name}) count is null")
                        return@forEach
                    }
                    icAmenities[it.name as CountableInternalAmenity] = it
                }
            }
        }

        _icAmenityMap.value = icAmenities
        _internalAmenityMap.value = internalAmenities
        _socialAmenityMap.value = societyAmenities
        InitialData.icAmenity = icAmenities
        InitialData.internalAmenity = internalAmenities
        InitialData.socialAmenity = societyAmenities
    }

    private fun bindImages(images: List<PropertyImage>) {
        _images.value = images
        InitialData.images = images
    }

    fun resetForm() {
        // Reset Main Result
        _submitPropertyResult.value = null

        // Reset Initial data
        InitialData.reset()

        // Reset Form Data
        propertyDataUI = InitialData.basic.copy()
        _images.value = InitialData.images
        // Reset Amenities
        _icAmenityMap.value = InitialData.icAmenity
        _internalAmenityMap.value = InitialData.internalAmenity
        _socialAmenityMap.value = InitialData.socialAmenity

        // Reset Form Error
        formErrorMap.forEach { (key, _) -> formErrorMap.getValue(key).value = null }
        logInfo("Form reset is done.")
    }

    private fun checkValidation(): Boolean {
        val propertyData = propertyDataUI
        var isValidationSuccess = true

        // Helper function to reduce repetition
        fun updateError(field: PropertyFormField, errorMessage: String) {
            val errorField = formErrorMap.getValue(field)
            errorField.value = errorMessage
            isValidationSuccess = false
        }

        // Name Validation
        when {
            propertyData.name.isEmpty() -> "enter valid input"
            propertyData.name.length < 3 -> "length should be greater than 3"
            propertyData.name.length > 100 -> "length should be less than 100"
            else -> null
        }?.let {
            updateError(PropertyFormField.NAME, it)
        }

        fun getDataByField(field: PropertyFormField) = when (field) {
            PropertyFormField.TYPE -> propertyData.type
            PropertyFormField.BHK -> propertyData.bhk
            PropertyFormField.FURNISHING_TYPE -> propertyData.furnishingType
            PropertyFormField.PREFERRED_TENANT_TYPE -> propertyData.preferredTenantTypes
            PropertyFormField.NAME -> propertyData.name
            PropertyFormField.DESCRIPTION -> propertyData.description
            PropertyFormField.KIND -> propertyData.kind
            PropertyFormField.PREFERRED_BACHELOR_TYPE -> propertyData.preferredBachelorType
            PropertyFormField.COVERED_PARKING_COUNT -> propertyData.countOfCoveredParking
            PropertyFormField.OPEN_PARKING_COUNT -> propertyData.countOfOpenParking
            PropertyFormField.AVAILABLE_FROM -> propertyData.availableFrom
            PropertyFormField.BUILT_UP_AREA -> propertyData.builtUpArea
            PropertyFormField.BATH_ROOM_COUNT -> propertyData.bathRoomCount
            PropertyFormField.IS_PET_FRIENDLY -> propertyData.isPetAllowed
            PropertyFormField.PRICE -> propertyData.price
            PropertyFormField.IS_MAINTENANCE_SEPARATE -> propertyData.isMaintenanceSeparate
            PropertyFormField.MAINTENANCE_CHARGES -> propertyData.maintenanceCharges
            PropertyFormField.SECURITY_DEPOSIT -> propertyData.securityDepositAmount
            PropertyFormField.STREET -> propertyData.street
            PropertyFormField.LOCALITY -> propertyData.locality
            PropertyFormField.CITY -> propertyData.city
            PropertyFormField.IMAGES -> null
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
            if (getDataByField(filed) == null)
                updateError(filed, "select one")
        }

        // MAINTENANCE_CHARGES Validation
        if (propertyData.isMaintenanceSeparate == true && propertyData.maintenanceCharges.isEmpty())
            updateError(PropertyFormField.MAINTENANCE_CHARGES, "enter valid input")

        // Int Input Validations
        listOf(
            PropertyFormField.BUILT_UP_AREA,
            PropertyFormField.PRICE,
            PropertyFormField.SECURITY_DEPOSIT
        ).forEach {
            val value = getDataByField(it)
            if (value !is String || value.toIntOrNull() == null)
                updateError(it, "enter valid input")
        }

        // String Input Validations
        listOf(
            PropertyFormField.CITY,
            PropertyFormField.LOCALITY,
            PropertyFormField.STREET,
            PropertyFormField.AVAILABLE_FROM,
        ).forEach {
            val value = getDataByField(it) as? String
            if (value.isNullOrEmpty())
                updateError(it, "enter valid input")
        }

        if (_images.value!!.isEmpty())
            updateError(PropertyFormField.IMAGES, "Upload at least one image")

        return isValidationSuccess
    }

    private fun builtPropertyDomain(currentUserId: Long, propertyId: Long = 0L) : Property? {
        try {
            val basicData = propertyDataUI
            val amenities = buildAmenities()
            val address = PropertyAddress(
                street = basicData.street,
                locality = basicData.locality,
                city = basicData.city
            )

            return Property(
                id = propertyId,
                landlordId = currentUserId,
                name = basicData.name,
                description = basicData.description,
                lookingTo = basicData.lookingTo,
                kind = PropertyKind.RESIDENTIAL,
                type = basicData.type!!,
                furnishingType = basicData.furnishingType!!,
                amenities = amenities,
                preferredTenantType = basicData.preferredTenantTypes!!,
                preferredBachelorType = basicData.preferredBachelorType,
                transactionType = null,
                ageOfProperty = 0,
                countOfCoveredParking = basicData.countOfCoveredParking.toInt(),
                countOfOpenParking = basicData.countOfOpenParking.toInt(),
                availableFrom = basicData.availableFrom.toEpochSeconds(),
                bhk = basicData.bhk!!,
                builtUpArea = basicData.builtUpArea.toInt(),
                bathRoomCount = basicData.bathRoomCount.toInt(),
                isPetAllowed = basicData.isPetAllowed!!,
                isActive = true,
                price = basicData.price.toInt(),
                isMaintenanceSeparate = basicData.isMaintenanceSeparate!!,
                maintenanceCharges = basicData.maintenanceCharges.toIntOrNull(),
                securityDepositAmount = basicData.securityDepositAmount.toInt(),
                address = address,
                images = _images.value!!,
                createdAt = System.currentTimeMillis()
            )
        } catch (exp: Exception) {
            logError(exp.message.toString(), exp)
            return null
        }
    }

    private fun buildAmenities(): List<AmenityDomain> = mutableListOf<AmenityDomain>().apply {
        _icAmenityMap.value?.let {  addAll(it.values) }
        _internalAmenityMap.value?.let { addAll(it.values) }
        _socialAmenityMap.value?.let { addAll(it.values) }
    }

    fun clearValidationError() {
        _validationError.value = null
    }

    fun isFormDirty(): Boolean {
        return propertyDataUI != InitialData.basic ||
        _images.value != InitialData.images ||
        _icAmenityMap.value != InitialData.icAmenity ||
        _internalAmenityMap.value != InitialData.internalAmenity ||
        _socialAmenityMap.value != InitialData.socialAmenity
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