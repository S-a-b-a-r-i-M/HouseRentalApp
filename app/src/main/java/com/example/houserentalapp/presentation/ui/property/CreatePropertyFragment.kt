package com.example.houserentalapp.presentation.ui.property

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import com.example.houserentalapp.R

import com.example.houserentalapp.databinding.FragmentCreatePropertyBinding
import com.example.houserentalapp.domain.model.enums.BHK
import com.example.houserentalapp.domain.model.enums.BachelorType
import com.example.houserentalapp.domain.model.enums.FurnishingType
import com.example.houserentalapp.domain.model.enums.PropertyType
import com.example.houserentalapp.domain.model.enums.TenantType
import com.example.houserentalapp.presentation.ui.MainActivity
import com.example.houserentalapp.presentation.ui.property.viewmodel.CreatePropertyViewModel
import com.example.houserentalapp.presentation.enums.PropertyFormField
import com.example.houserentalapp.presentation.ui.common.CounterView
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.createPropertyViewModelFactory
import com.example.houserentalapp.presentation.utils.extensions.dpToPx
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.logWarning
import com.example.houserentalapp.presentation.utils.extensions.showToast
import com.example.houserentalapp.presentation.utils.helpers.getRequiredStyleLabel
import com.example.houserentalapp.presentation.utils.helpers.setSystemBarBottomPadding
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.collections.component1
import kotlin.collections.component2

/* TODO:
    1. FIX: Property description, issue: horizontal scroll
    2. Reset Form
    3. Validation for Maintenance charges
    4. Image Upload (Click From Camera , Upload From Gallery)
    5. Change toolbar implementation to built in
*/

class CreatePropertyFragment : Fragment(R.layout.fragment_create_property) {
    private lateinit var binding: FragmentCreatePropertyBinding
    private lateinit var mainActivity: MainActivity

    private val amenitiesBottomSheet by lazy { AmenitiesBottomSheet() }
    private val imagesBottomSheet by lazy { PropertyImagesBottomSheet() }
    private val myDatePicker by lazy { getDatePicker() }
    private val viewModel: CreatePropertyViewModel by activityViewModels {
        createPropertyViewModelFactory()
    }

    private val formTextInputFieldInfoList = mutableListOf<TextInputFieldInfo>()
    private val formCounterViewList = mutableListOf<Pair<PropertyFormField, CounterView>>()
    private val formSingleSelectChipGroupsInfo = mutableListOf<SingleSelectableChipGroupInfo>()

    private val imagesPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { it ->
        if (it.resultCode == RESULT_OK && it.data != null) {
            // TODO: handle max images
            val clipData = it.data?.clipData
            val imageUris = mutableListOf<Uri>()

            if (clipData != null)
                // Multiple images selected
                for (i in 0 until clipData.itemCount)
                    imageUris.add(clipData.getItemAt(i).uri)
            else
                // Single images selected
                it.data?.data?.let { uri ->  imageUris.add(uri) }

            if (imageUris.isNotEmpty()) {
                viewModel.setPropertyImages(imageUris)
                requireActivity().showToast("${imageUris.size} selected successfully")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCreatePropertyBinding.bind(view)
        mainActivity = context as MainActivity

        // Set up UI
        setupUI()

        // Set Listeners
        setListeners()

        // Observe View Model
         observeViewModel()
    }

    private val chipIdToBHK = mapOf(
        R.id.chip1BHK to BHK.ONE_BHK,
        R.id.chip2BHK to BHK.TWO_BHK,
        R.id.chip3BHK to BHK.THREE_BHK,
        R.id.chip4BHK to BHK.FOUR_BHK,
        R.id.chip5BHK to BHK.FIVE_PLUS_BHK,
        R.id.chip5aboveBHK to BHK.FIVE_PLUS_BHK,
    )
    private val bhkToChipId = chipIdToBHK.entries.associate { (k, v) -> v to k }

    private val chipIdToPropertyType = mapOf(
        R.id.chipApartment to PropertyType.APARTMENT,
        R.id.chipVilla to PropertyType.VILLA,
        R.id.chipIndependentHouse to PropertyType.INDEPENDENT_HOUSE,
        R.id.chipFarmHouse to PropertyType.FARM_HOUSE,
        R.id.chipStudio to PropertyType.STUDIO,
        R.id.chipOther to PropertyType.OTHER,
    )
    private val propertyTypeToChipId = chipIdToPropertyType.entries.associate { (k, v) -> v to k }

    private val chipIdToFurnishingType = mapOf(
        R.id.chipFurnished to FurnishingType.FULLY_FURNISHED,
        R.id.chipUnFurnished to FurnishingType.UN_FURNISHED,
        R.id.chipSemiFurnished to FurnishingType.SEMI_FURNISHED,
    )
    private val furnishingTypeToChipId = chipIdToFurnishingType.entries.associate { (k, v) -> v to k }

    private val chipIdToTenantType = mapOf(
        R.id.chipFamily to TenantType.FAMILY,
        R.id.chipBachelor to TenantType.BACHELORS
    )
    private val tenantTypeToChipId = chipIdToTenantType.entries.associate { (k, v) -> v to k }

    private val chipIdToBachelorType = mapOf(
        R.id.chipOpenForBoth to BachelorType.BOTH,
        R.id.chipOnlyMen to BachelorType.MEN,
        R.id.chipOnlyWomen to BachelorType.WOMEN,
    )
    private val bachelorTypeToChipId = chipIdToBachelorType.entries.associate { (k, v) -> v to k }

    private fun observeErrors() {
        formTextInputFieldInfoList.forEach { fieldInfo ->
            if (fieldInfo.isRequired)
                viewModel.getFormErrorMap(fieldInfo.field).observe(viewLifecycleOwner) { error ->
                    fieldInfo.inputLayout.error = error
                    logDebug("observeEditTextErrors -> observeError -> ${fieldInfo.field}: $error")
                }
        }

        fun getErrorState(err: String?) = resources.getColor(
            if (err != null) R.color.red_error else R.color.gray_dark
        )

        formSingleSelectChipGroupsInfo.forEach {
            viewModel.getFormErrorMap(it.field).observe(viewLifecycleOwner) { error ->
                it.label.setTextColor(getErrorState(error))
                logDebug("observeChipGroupErrors -> observeError -> ${it.field}: $error")
            }
        }

        viewModel.getFormErrorMap(PropertyFormField.PREFERRED_TENANT_TYPE).observe(viewLifecycleOwner) {
            binding.tvTenantType.setTextColor(getErrorState(it))
        }
    }

    private fun observePropertyBasic() {
        with(binding) {
            viewModel.propertyBasicUI.observe(viewLifecycleOwner) { data ->
                if (etPropertyName.text.toString() != data.name)
                    etPropertyName.setText(data.name)
                if (etPropertyDes.text.toString() == data.description)
                    etPropertyDes.setText(data.description)
                if (data.type != null)
                    chipGroupPropertyType.check(propertyTypeToChipId.getValue(data.type))
                if (data.bhk != null)
                    chipGroupBHK.check(bhkToChipId.getValue(data.bhk))
//                if (etBuiltUpArea.text.toString() != data.builtUpArea.toString())
//                    etBuiltUpArea.setText(data.builtUpArea.toString())
                if (data.bathRoomCount?.toIntOrNull() != bathRoomCounter.count)
                    bathRoomCounter.count = data.bathRoomCount?.toIntOrNull() ?: 0
            }
        }
    }

    private fun observePropertyPreferences() {
        with(binding) {
            viewModel.propertyPreferencesUI.observe(viewLifecycleOwner) {
                if (it.furnishingType != null)
                    chipGroupFurnishing.check(furnishingTypeToChipId.getValue(it.furnishingType))
                it.preferredTenantTypes?.forEach { tenant ->
                    chipGroupTenantType.check(tenantTypeToChipId.getValue(tenant))
                }
                if (it.preferredBachelorType != null)
                    chipGroupBachelorPreference.check(bachelorTypeToChipId.getValue(it.preferredBachelorType))
                if (it.isPetAllowed != null) {
                    if (it.isPetAllowed)
                        chipPetFriendlyYes.isChecked = true
                    else
                        chipPetFriendlyNo.isChecked = true
                }
                if (it.countOfCoveredParking?.toIntOrNull() != coveredParkingCounter.count)
                    coveredParkingCounter.count = it.countOfCoveredParking?.toIntOrNull() ?: 0
                if (it.countOfOpenParking?.toIntOrNull() != openParkingCounter.count)
                    openParkingCounter.count = it.countOfOpenParking?.toIntOrNull() ?: 0
                if (it.availableFrom != etAvailableFrom.text.toString())
                    etAvailableFrom.setText(it.availableFrom)
            }
        }
    }

    private fun observePropertyPricing() {
        with(binding) {
            viewModel.propertyPricingUI.observe(viewLifecycleOwner) {
                if (it.price != null)
                    etPrice.setText(it.price)
                if (it.isMaintenanceSeparate != null) {
                    if (it.isMaintenanceSeparate)
                        chipSeparate.isChecked = true
                    else
                        chipIncludeInRent.isChecked = true
                }
                if (it.maintenanceCharges != null)
                    etMaintenanceCharge.setText(it.maintenanceCharges)
                if (it.securityDepositAmount != null)
                    etSecurityDeposit.setText(it.securityDepositAmount)
            }
        }
    }

    private fun observePropertyAddress() {
        with(binding) {
            viewModel.propertyAddressUI.observe(viewLifecycleOwner) {
                if (it.street != null)
                    etStreet.setText(it.street)
                if (it.locality != null)
                    etLocality.setText(it.locality)
                if (it.city != null)
                    etCity.setText(it.city)
            }
        }
    }

    private fun observeImageUri() {
        with(binding) {
            viewModel.imageUris.observe(viewLifecycleOwner) { uris ->
                if (uris.isEmpty()) {
                    cvUploadImages.visibility = View.VISIBLE
                    llUploadedImages.visibility = View.GONE
                } else {
                    llUploadedImages.visibility = View.VISIBLE
                    cvUploadImages.visibility = View.GONE

                    // Load Images
                    val context = requireActivity()
                    val imageWidth = ((context.resources.displayMetrics.widthPixels) / 4.2).toInt()
                    val params = LinearLayout.LayoutParams(
                        imageWidth,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                    ).apply { setMargins(4, 0, 4, 0) }

                    // Display Only 3 Images
                    llUploadedImages.removeAllViews()
                    for (i in 0 until uris.size.coerceAtMost(3)) {
                        val imageView = ShapeableImageView(context).apply {
                            setImageURI(uris[i])
                            scaleType = ImageView.ScaleType.CENTER_CROP
                            shapeAppearanceModel = ShapeAppearanceModel.builder()
                                .setAllCornerSizes(12f)
                                .build()
                            setLayoutParams(params)
                        }

                        // Add Image to the view
                        llUploadedImages.addView(imageView)
                    }

                    // Add action button
                    val button = MaterialButton(context).apply {
                        textSize = 14f
                        setTextColor(context.resources.getColor(R.color.primary_blue_dark))
                        backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)

                        setPadding(0)
                        setLayoutParams(
                            LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                            ).apply { setMargins(5.dpToPx(context)) }
                        )
                    }

                    if (uris.size > 3) {
                        button.apply {
                            text = "${uris.size - 3}+ Images"
                            // On Click Event
                            setOnClickListener {
                                imagesBottomSheet.show(parentFragmentManager, "imagesBottomSheet")
                                logInfo("view images clicked.......")
                            }
                        }
                    } else {
                        button.apply {
                            text = "+ Add"
                            // On Click Event
                            setOnClickListener {
                                imagesBottomSheet.show(parentFragmentManager, "imagesBottomSheet")
                                logInfo("add more clicked.......")
                            }
                        }
                    }
                    // Add Button to the view
                    llUploadedImages.addView(button)
                }
            }
        }
    }

    private fun observeViewModel() {
        observeErrors()
        observePropertyBasic()
        observePropertyPreferences()
        observePropertyPricing()
        observePropertyAddress()
        observeImageUri()

        // Creation Result
        viewModel.createPropertyResult.observe(viewLifecycleOwner) { result ->
            if (result == null) return@observe

            when(result) {
                is ResultUI.Success<Long> -> {
                    hideProgressBar()
                    hideError()

                    requireActivity().showToast("Property posted successfully")
                    viewModel.resetForm()
                    parentFragmentManager.popBackStack()
                }
                is ResultUI.Error -> {
                    hideProgressBar()
                    showError()
                    requireActivity().showToast("Failed ❌")
                }
                ResultUI.Loading -> {
                    hideError()
                    showProgressBar()
                }
            }
        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showError() {

    }

    private fun hideError() {

    }

    private fun setupUI() {
        // Always hide bottom nav
        mainActivity.hideBottomNav()

        // Add paddingBottom to avoid system bar overlay
        setSystemBarBottomPadding(binding.root)

        setupCustomToolBar()
        groupRelatedFields()
        setRequiredFieldIndicator()
    }

    private fun groupRelatedFields() {
        // Grouping Related Fields with views
        with(binding) {
            formTextInputFieldInfoList.apply {
                add(TextInputFieldInfo(
                    PropertyFormField.NAME,
                    tilPropertyName,
                    etPropertyName
                ))
                add(TextInputFieldInfo(
                    PropertyFormField.DESCRIPTION,
                    tilPropertyDes,
                    etPropertyDes,
                    false
                ))
                add(TextInputFieldInfo(
                    PropertyFormField.AVAILABLE_FROM,
                    tilAvailableFrom,
                    etAvailableFrom
                ))
                add(TextInputFieldInfo(
                    PropertyFormField.BUILT_UP_AREA,
                    tilBuiltUpArea,
                    etBuiltUpArea
                ))
                add(TextInputFieldInfo(
                    PropertyFormField.PRICE,
                    tilPrice,
                    etPrice
                ))
                add(TextInputFieldInfo(
                    PropertyFormField.MAINTENANCE_CHARGES,
                    tilMaintenanceCharge,
                    etMaintenanceCharge
                ))
                add(TextInputFieldInfo(
                    PropertyFormField.SECURITY_DEPOSIT,
                    tilSecurityDeposit,
                    etSecurityDeposit
                ))
                add(TextInputFieldInfo(
                    PropertyFormField.STREET, tilStreet, etStreet
                ))
                add(TextInputFieldInfo(
                    PropertyFormField.LOCALITY, tilLocality, etLocality
                ))
                add(TextInputFieldInfo(
                    PropertyFormField.CITY, tilCity, etCity
                ))
            }

            formCounterViewList.apply {
                add(Pair(PropertyFormField.COVERED_PARKING_COUNT, coveredParkingCounter))
                add(Pair(PropertyFormField.OPEN_PARKING_COUNT, openParkingCounter))
                add(Pair(PropertyFormField.BATH_ROOM_COUNT, bathRoomCounter))
            }

            formSingleSelectChipGroupsInfo.apply {
                add(SingleSelectableChipGroupInfo(
                    PropertyFormField.LOOKING_TO, tvLookingTo, chipGroupLookingTo
                ))
                add(SingleSelectableChipGroupInfo(
                    PropertyFormField.TYPE, tvPropertyType, chipGroupPropertyType
                ))
                add(SingleSelectableChipGroupInfo(
                    PropertyFormField.BHK, tvBHK, chipGroupBHK
                ))
                add(SingleSelectableChipGroupInfo(
                    PropertyFormField.FURNISHING_TYPE, tvFurnishing, chipGroupFurnishing
                ))
                add(SingleSelectableChipGroupInfo(
                    PropertyFormField.IS_PET_FRIENDLY, tvPetFriendly, chipGroupPetFriendly
                ))
                add(SingleSelectableChipGroupInfo(
                    PropertyFormField.PREFERRED_BACHELOR_TYPE, tvPreferredBachelor, chipGroupBachelorPreference
                ))
                add(SingleSelectableChipGroupInfo(
                    PropertyFormField.IS_MAINTENANCE_SEPARATE, tvMaintenanceSeparate, chipGroupMaintenance
                ))
            }
        }
    }

    private fun setRequiredFieldIndicator() {
        with(binding) {
            formTextInputFieldInfoList.forEach {
                if (it.isRequired)
                    it.inputLayout.hint = getRequiredStyleLabel(
                        it.inputLayout.hint.toString(), mainActivity
                    )
            }

            formSingleSelectChipGroupsInfo.forEach {
                if (it.isRequired)
                    it.label.text = getRequiredStyleLabel(
                        it.label.text.toString(), mainActivity
                    )
            }

            tvTenantType.text = getRequiredStyleLabel(
                tvTenantType.text.toString(), mainActivity
            )
        }
    }

    private fun setupCustomToolBar() {
        with(binding) {
            toolbar.title = getString(R.string.create_property)

            toolbar.setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun setEditTextListeners() {
        formTextInputFieldInfoList.forEach{ textInputFieldInfo ->
            textInputFieldInfo.editText.addTextChangedListener {
                viewModel.updateFormValue2(
                    textInputFieldInfo.field,
                    textInputFieldInfo.editText.text.toString()
                )
            }
        }
    }

    private fun setSingleSelectableChipGroupListeners() {
        fun updateValueInViewModel(field: PropertyFormField, chipId: Int) = when(field) {
            PropertyFormField.BHK -> viewModel.updateBHK(chipIdToBHK.getValue(chipId))
            PropertyFormField.LOOKING_TO -> logWarning("LOOKING_TO Not yet handled")
            PropertyFormField.TYPE -> viewModel.updatePropertyType(chipIdToPropertyType.getValue(chipId))
            PropertyFormField.FURNISHING_TYPE -> viewModel.updateFurnishing(chipIdToFurnishingType.getValue(chipId))
            PropertyFormField.IS_PET_FRIENDLY -> viewModel.updateFormValue2(field, chipId == R.id.chipPetFriendlyYes)
            PropertyFormField.PREFERRED_BACHELOR_TYPE -> viewModel.updatePreferredBachelor(chipIdToBachelorType.getValue(chipId))
            PropertyFormField.IS_MAINTENANCE_SEPARATE -> viewModel.updateFormValue2(field, chipId == R.id.chipSeparate)
            else -> logWarning("Invalid PropertyFormField for updateValueInViewModel")
        }

        formSingleSelectChipGroupsInfo.forEach {
            it.chipGroup.setOnCheckedStateChangeListener { chipGroup, checkedIds ->
                if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
                updateValueInViewModel(it.field, checkedIds[0])
            }
        }

        binding.chipSeparate.setOnCheckedChangeListener { _, isChecked ->
            onMaintenanceSeparateSelect(isChecked)
        }
    }

    private fun setListeners() {
        setEditTextListeners()
        setSingleSelectableChipGroupListeners()
        setCounterViewsListeners()
        setPreferredTenantListeners()

        // Other Listeners
        with(binding) {
            // Available From Date Picker
            etAvailableFrom.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
                if (hasFocus) myDatePicker.show(parentFragmentManager, "DATE_PICKER")
            }

            // Open Amenities Sheet
            btnAddAmenities.setOnClickListener {
                amenitiesBottomSheet.show(parentFragmentManager, "AmenitiesBottomSheet")
            }

            // Pick Images
            btnUploadImages.setOnClickListener {
                // action will tell what exactly we are intent to do.
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) //
                }
                imagesPickerLauncher.launch(intent)
            }

            // Save Button
            btnPostProperty.setOnClickListener {
                viewModel.createProperty()
            }
        }
    }

    private fun setCounterViewsListeners() {
        fun getCurrentValue(filed: PropertyFormField) = when (filed) {
            PropertyFormField.COVERED_PARKING_COUNT -> viewModel.propertyPreferencesUI.value?.countOfCoveredParking
            PropertyFormField.OPEN_PARKING_COUNT -> viewModel.propertyPreferencesUI.value?.countOfOpenParking
            PropertyFormField.BATH_ROOM_COUNT -> viewModel.propertyBasicUI.value?.bathRoomCount
            else -> null
        }

        with(binding) {
            // Parking, BathRoom
            formCounterViewList.forEach { (field, counterView) ->
                counterView.apply {
                    onCountIncrementListener = {
                        val newValue = (getCurrentValue(field)?.toIntOrNull() ?: 0) + 1
                        viewModel.updateFormValue2(field , newValue.toString())
                    }
                    onCountDecrementListener = {
                        val newValue = (getCurrentValue(field)?.toIntOrNull() ?: 0) - 1
                        viewModel.updateFormValue2(field , newValue.toString())
                    }
                }
            }
        }
    }

    private fun setPreferredTenantListeners() {
        with(binding) {
            // Handling Preferred Tenant Selection
            chipGroupTenantType.setOnCheckedStateChangeListener { _, checkedIds ->
                val tenants = mutableListOf<TenantType>()
                if (R.id.chipFamily in checkedIds)
                    tenants.add(TenantType.FAMILY)
                else if (R.id.chipBachelors in checkedIds)
                    tenants.add(TenantType.BACHELORS)

                viewModel.updatePreferredTenants(tenants)
            }

            chipBachelors.setOnCheckedChangeListener { _, isChecked ->
                onBachelorsSelectChange(isChecked)
            }
        }
    }

    private fun onBachelorsSelectChange(isSelected: Boolean) {
        logInfo("<------------ onBachelorsSelectChange: $isSelected ------------>")
        if (isSelected) {
            binding.preferredBachelorContainer.visibility = View.VISIBLE
            binding.chipOpenForBoth.isChecked = true // Note: It won't invoke onclick of btnOpenForBoth
        } else
            binding.preferredBachelorContainer.visibility = View.GONE
    }

    private fun onMaintenanceSeparateSelect(isSelected: Boolean) {
        logInfo("<------------ onMaintenanceSeparateSelect: $isSelected ------------>")
        binding.tilMaintenanceCharge.visibility = if (isSelected) View.VISIBLE else View.GONE
    }

    private fun getDatePicker():  MaterialDatePicker<Long> {
        // TO DISABLE PAST DATES
        val calendarConstraints = CalendarConstraints.Builder()
            .setValidator(
                DateValidatorPointForward.from(
                    System.currentTimeMillis() - (24 * 60 * 60 * 1000)
                )
            )

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setCalendarConstraints(calendarConstraints.build())
            .build()

        // HANDLING EVENTS
        datePicker.addOnNegativeButtonClickListener {
            binding.etAvailableFrom.clearFocus()
        }

        datePicker.addOnCancelListener {
            binding.etAvailableFrom.clearFocus()
        }

        datePicker.addOnPositiveButtonClickListener { selectedDateEpoch ->
            val formattedDate = SimpleDateFormat(
                "dd/MM/yyyy", Locale.getDefault()
            ).format(selectedDateEpoch)

            // SET DATE TO EDITTEXT
            binding.etAvailableFrom.setText(formattedDate)
            binding.etAvailableFrom.clearFocus()
        }

        return datePicker
    }

    override fun onDetach() {
        super.onDetach()
        mainActivity.showBottomNav()
    }

    private data class TextInputFieldInfo (
        val field: PropertyFormField,
        val inputLayout: TextInputLayout,
        val editText: TextInputEditText,
        val isRequired: Boolean = true
    )

    private data class SingleSelectableChipGroupInfo (
        val field: PropertyFormField,
        val label: TextView,
        val chipGroup: ChipGroup,
        val isRequired: Boolean = true
    )
}