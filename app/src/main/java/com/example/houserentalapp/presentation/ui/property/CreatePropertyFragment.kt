package com.example.houserentalapp.presentation.ui.property

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentCreatePropertyBinding
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.model.enums.BHK
import com.example.houserentalapp.domain.model.enums.BachelorType
import com.example.houserentalapp.domain.model.enums.FurnishingType
import com.example.houserentalapp.domain.model.enums.PropertyType
import com.example.houserentalapp.domain.model.enums.TenantType
import com.example.houserentalapp.presentation.ui.property.viewmodel.CreatePropertyViewModel
import com.example.houserentalapp.presentation.enums.PropertyFormField
import com.example.houserentalapp.presentation.model.PropertyDataUI
import com.example.houserentalapp.presentation.ui.BundleKeys
import com.example.houserentalapp.presentation.ui.ResultRequestKeys
import com.example.houserentalapp.presentation.ui.common.CounterView
import com.example.houserentalapp.presentation.ui.interfaces.BottomNavController
import com.example.houserentalapp.presentation.ui.sharedviewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.MaterialColorAttr
import com.example.houserentalapp.presentation.utils.extensions.createPropertyViewModelFactory
import com.example.houserentalapp.presentation.utils.extensions.dpToPx
import com.example.houserentalapp.presentation.utils.extensions.getThemeColor
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.logWarning
import com.example.houserentalapp.presentation.utils.extensions.showToast
import com.example.houserentalapp.presentation.utils.helpers.ImageUploadHelper
import com.example.houserentalapp.presentation.utils.helpers.getRequiredStyleLabel
import com.example.houserentalapp.presentation.utils.helpers.loadImageSourceToImageViewV2
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
import kotlin.getValue

class CreatePropertyFragment : Fragment(R.layout.fragment_create_property) {
    private val _context: Context get() = requireContext()
    private lateinit var bottomNavController: BottomNavController
    private lateinit var binding: FragmentCreatePropertyBinding
    private lateinit var currentUser: User
    private lateinit var imageUploadHelper: ImageUploadHelper

    private val amenitiesBottomSheet by lazy { AmenitiesBottomSheet() }
    private val imagesBottomSheet by lazy { PropertyImagesBottomSheet() }
    private val myDatePicker by lazy { getDatePicker() }
    private val viewModel: CreatePropertyViewModel by activityViewModels {
        createPropertyViewModelFactory()
    }
    private val sharedDataViewModel: SharedDataViewModel by activityViewModels()
    private val formTextInputFieldInfoList = mutableListOf<TextInputFieldInfo>()
    private val formCounterViewList = mutableListOf<Pair<PropertyFormField, CounterView>>()
    private val formSingleSelectChipGroupsInfo = mutableListOf<SingleSelectableChipGroupInfo>()
    private var propertyIdToEdit: Long? = null
    private var hideAndShowBottomNav: Boolean = false


    override fun onAttach(context: Context) {
        super.onAttach(context)
        bottomNavController = context as BottomNavController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        propertyIdToEdit = arguments?.getLong(BundleKeys.PROPERTY_ID)
        if (propertyIdToEdit == 0L)  propertyIdToEdit = null
        hideAndShowBottomNav = arguments?.getBoolean(BundleKeys.HIDE_AND_SHOW_BOTTOM_NAV)
            ?: hideAndShowBottomNav
        // Take Current User
        currentUser = sharedDataViewModel.currentUserData
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCreatePropertyBinding.bind(view)

        // Set up UI
        setupUI()

        // Set Listeners
        setListeners()

        // Observe View Model
        observeViewModel()

        // Handle Back Press
        addBackPressCallBack()

        // Image Helper
        setImageUploadHelper()

        if (savedInstanceState == null)
            propertyIdToEdit?.let { // If Edit Mode
                viewModel.loadPropertyToEdit(it, ::onEditPropertyLoaded)
            }
    }

    private fun setImageUploadHelper() {
        imageUploadHelper = ImageUploadHelper().init(
            this,
            onImageFromCamera = ::handleCameraResult,
            onImageFromPicker = ::handleImagePrickerResult,
            onPermissionDenied = ::onCameraPermissionDenied,
        )
        imageUploadHelper.multipleImagesFromPicker = true // Allow to pick multiple images
    }

    private fun handleImagePrickerResult(intent: Intent) {
        val clipData = intent.clipData
        val imageUris = mutableListOf<Uri>()

        if (clipData != null)
        // Multiple images selected
            for (i in 0 until clipData.itemCount)
                imageUris.add(clipData.getItemAt(i).uri)
        else
        // Single image selected
            intent.data?.let { uri ->  imageUris.add(uri) }

        if (imageUris.isNotEmpty()) {
            viewModel.addPropertyImages(imageUris)
            requireActivity().showToast("${imageUris.size} selected successfully")
        }
    }

    private fun handleCameraResult(uri: Uri) {
        logInfo("got image $uri")
        viewModel.addPropertyImage(uri)
        showAnotherPhotoDialog()
    }

    private fun onCameraPermissionDenied() {
        logInfo("User denied the camera permission")
        _context.showToast("Please provide camera permission to take pictures")
    }

    private fun addBackPressCallBack() {
        (_context as AppCompatActivity).onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleOnBackNavigation()
                }
            })
    }

    private fun handleOnBackNavigation() {

        if (viewModel.isFormDirty())
            AlertDialog.Builder(_context)
                .setTitle("Discard Changes")
                .setMessage("Are you sure you want to discard the changes ?")
                .setPositiveButton("Discard") { _, _ ->
                    viewModel.resetForm()
                    parentFragmentManager.popBackStack()
                }
                .setNegativeButton("Cancel", null)
                .show()
        else {
            viewModel.resetForm()
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupUI() {
        // Always hide bottom nav
        if (hideAndShowBottomNav)
            bottomNavController.hideBottomNav()

        // Add paddingBottom to avoid system bar overlay
        setSystemBarBottomPadding(binding.root) // TODO-FIX:

        groupRelatedFields()
        setRequiredFieldIndicator()

        with(binding) {
            var titleText = R.string.create_property
            var submitButtonText = R.string.post_property
            if (propertyIdToEdit != null) {
                titleText = R.string.edit_property
                submitButtonText = R.string.save_changes
            }

            toolbar.title = getString(titleText)
            btnSubmit.text = getString(submitButtonText)
            toolbar.setNavigationOnClickListener { handleOnBackNavigation() }

            @SuppressLint("ClickableViewAccessibility")
            etPropertyDes.setOnTouchListener { v, event ->
                v.parent.requestDisallowInterceptTouchEvent(true)
                false
            }
        }
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
                if (it.field.isRequired)
                    it.inputLayout.hint = getRequiredStyleLabel(
                        it.inputLayout.hint.toString(), _context
                    )
            }

            formSingleSelectChipGroupsInfo.forEach {
                if (it.field.isRequired)
                    it.label.text = getRequiredStyleLabel(
                        it.label.text.toString(), _context
                    )
            }

            tvTenantType.text = getRequiredStyleLabel(
                tvTenantType.text.toString(), _context
            )
        }
    }

    private val chipIdToBHK = mapOf(
        R.id.chip1RK to BHK.ONE_RK,
        R.id.chip1BHK to BHK.ONE_BHK,
        R.id.chip2BHK to BHK.TWO_BHK,
        R.id.chip3BHK to BHK.THREE_BHK,
        R.id.chip4BHK to BHK.FOUR_BHK,
        // R.id.chip5BHK to BHK.FIVE_PLUS_BHK,
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
        R.id.chipFullyFurnished to FurnishingType.FULLY_FURNISHED,
        R.id.chipUnFurnished to FurnishingType.UN_FURNISHED,
        R.id.chipSemiFurnished to FurnishingType.SEMI_FURNISHED,
    )
    private val furnishingTypeToChipId = chipIdToFurnishingType.entries.associate { (k, v) -> v to k }

    private val chipIdToTenantType = mapOf(
        R.id.chipFamily to TenantType.FAMILY,
        R.id.chipBachelors to TenantType.BACHELORS
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
            if (fieldInfo.field.isRequired)
                viewModel.getFormErrorMap(fieldInfo.field).observe(viewLifecycleOwner) { error ->
                    fieldInfo.inputLayout.error = error
                    logDebug("observeEditTextErrors -> observeError -> ${fieldInfo.field}: $error")
                }
        }

        fun getErrorState(err: String?) = resources.getColor(
            if (err != null) R.color.red_error else R.color.gray_dark
        )

        formSingleSelectChipGroupsInfo.forEach {
        if (it.field.isRequired)
            viewModel.getFormErrorMap(it.field).observe(viewLifecycleOwner) { error ->
                it.label.setTextColor(getErrorState(error))
                logDebug("observeChipGroupErrors -> observeError -> ${it.field}: $error")
            }
        }

        viewModel.getFormErrorMap(PropertyFormField.PREFERRED_TENANT_TYPE).observe(viewLifecycleOwner) {
            binding.tvTenantType.setTextColor(getErrorState(it))
        }
    }

    private fun updateTextInput(editText: TextInputEditText, value: String) {
        if (value != editText.text.toString())
            editText.setText(value)
    }

    private fun updateCount(counterView: CounterView, value: String) {
        val count = value.toIntOrNull()
        if (count != null && count != counterView.count)
            counterView.count = value.toIntOrNull() ?: 0
    }

    private fun onEditPropertyLoaded(dataUI: PropertyDataUI) {
        with(binding) {
            updateTextInput(etPropertyName, dataUI.name)
            updateTextInput(etPropertyDes, dataUI.description)
            updateTextInput(etBuiltUpArea, dataUI.builtUpArea)
            updateTextInput(etPrice, dataUI.price)
            updateTextInput(etMaintenanceCharge, dataUI.maintenanceCharges)
            updateTextInput(etSecurityDeposit, dataUI.securityDepositAmount)
            updateTextInput(etAvailableFrom, dataUI.availableFrom)
            updateTextInput(etStreet, dataUI.street)
            updateTextInput(etLocality, dataUI.locality)
            updateTextInput(etCity, dataUI.city)

            updateCount(bathRoomCounter, dataUI.bathRoomCount)
            updateCount(coveredParkingCounter, dataUI.countOfCoveredParking)
            updateCount(openParkingCounter, dataUI.countOfOpenParking)

            dataUI.type?.let {
                chipGroupPropertyType.check(propertyTypeToChipId.getValue(it))
            }
            dataUI.bhk?.let { chipGroupBHK.check(bhkToChipId.getValue(it)) }
            dataUI.furnishingType?.let {
                chipGroupFurnishing.check(furnishingTypeToChipId.getValue(it))
            }
            dataUI.preferredBachelorType?.let {
                chipGroupBachelorPreference.check(bachelorTypeToChipId.getValue(it))
            }

            dataUI.isMaintenanceSeparate?.let {
                if (it) chipSeparate.isChecked = true else chipIncludeInRent.isChecked = true
            }

            dataUI.isPetAllowed?.let {
                if (it) chipPetFriendlyYes.isChecked = true else chipPetFriendlyNo.isChecked = true
            }

            dataUI.preferredTenantTypes?.forEach { tenant ->
                chipGroupTenantType.check(tenantTypeToChipId.getValue(tenant))
            }
        }
    }

    private fun observeImageUri() {
        with(binding) {
            viewModel.images.observe(viewLifecycleOwner) { propertyImages ->
                if (propertyImages.isEmpty()) {
                    cvUploadImages.visibility = View.VISIBLE
                    cvTakeImages.visibility = View.VISIBLE
                    tvOR.visibility = View.VISIBLE
                    llUploadedImages.visibility = View.GONE
                } else {
                    llUploadedImages.visibility = View.VISIBLE
                    tvOR.visibility = View.GONE
                    cvTakeImages.visibility = View.GONE
                    cvUploadImages.visibility = View.GONE

                    // Load Images
                    val context = requireActivity()
                    val imageWidth = ((context.resources.displayMetrics.widthPixels) / 4.1).toInt()
                    val params = LinearLayout.LayoutParams(
                        imageWidth,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                    ).apply { setMargins(4, 0, 4, 0) }

                    // Display Only 3 Images
                    llUploadedImages.removeAllViews()
                    val maxImageDisplaySize = 3
                    for (i in 0 until propertyImages.size.coerceAtMost(maxImageDisplaySize)) {
                        val imageView = ShapeableImageView(context).apply {
                            scaleType = ImageView.ScaleType.CENTER_CROP
                            shapeAppearanceModel = ShapeAppearanceModel.builder()
                                .setAllCornerSizes(12f)
                                .build()
                            // On Click Event
                            setOnClickListener {
                                imagesBottomSheet.show(parentFragmentManager, "imagesBottomSheet")
                            }

                            setLayoutParams(params)
                        }
                        loadImageSourceToImageViewV2(propertyImages[i].imageSource, imageView)

                        // Add Image to the view
                        llUploadedImages.addView(imageView)
                    }

                    // Add button
                    if (propertyImages.size > maxImageDisplaySize) {
                        val button = MaterialButton(context).apply {
                            text = context.getString(
                                R.string.number_of_images,
                                propertyImages.size - maxImageDisplaySize
                            )
                            textSize = 14f
                            setTextColor(
                                context.getThemeColor(MaterialColorAttr.COLOR_PRIMARY_DARK)
                            )
                            backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)

                            // On Click Event
                            setOnClickListener {
                                imagesBottomSheet.show(parentFragmentManager, "imagesBottomSheet")
                            }

                            setPadding(0)
                            setLayoutParams(
                                LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                ).apply { setMargins(5.dpToPx()) }
                            )
                        }
                        // Add Button to the view
                        llUploadedImages.addView(button)
                    }
                }
            }
        }
    }

    private fun observeViewModel() {
        observeErrors()
        observeImageUri()

        with(viewModel) {
            // Creation Result
            submitPropertyResult.observe(viewLifecycleOwner) { result ->
                if (result == null) return@observe

                when(result) {
                    is ResultUI.Success<*> -> {
                        hideProgressBar()
                        hideError()
                        resetForm()

                        var message = "Property posted successfully"
                        propertyIdToEdit?.let{
                            sharedDataViewModel.setUpdatedPropertyId(it) // Using Shared View Model
                            parentFragmentManager.setFragmentResult( // Using Fragment Result API
                                ResultRequestKeys.IS_PROPERTY_MODIFIED,
                                Bundle().apply { putBoolean(BundleKeys.IS_PROPERTY_MODIFIED, true) }
                            )
                            message = "Property updated successfully"
                        }
                        _context.showToast(message)
                        parentFragmentManager.popBackStack()
                    }
                    is ResultUI.Error -> {
                        hideProgressBar()
                        showError("Unexpected Error occurred, try later.")
                        _context.showToast("Unexpected Error❌ occurred, please try later.")
                    }
                    ResultUI.Loading -> {
                        hideError()
                        showProgressBar()
                        if (propertyIdToEdit == null)
                            binding.btnSubmit.text = getString(R.string.posting)
                        else
                            binding.btnSubmit.text = getString(R.string.saving)
                    }
                }
            }

            validationError.observe(viewLifecycleOwner) {
                if (it != null) {
                    showError(getString(R.string.please_resolve_all_the_errors_msg))
                    requestFocus()
                    clearValidationError()
                }
            }
        }
    }

    // NOTE: ORDERED BY VISIBILITY
    private val fieldWithViewMap by lazy { linkedMapOf(
        PropertyFormField.CITY to binding.tilCity,
        PropertyFormField.LOCALITY to binding.tilLocality,
        PropertyFormField.STREET to binding.tilStreet,
        PropertyFormField.TYPE to binding.tvPropertyType,
        PropertyFormField.BHK to binding.tvBHK,
        PropertyFormField.NAME to binding.tilPropertyName,
        PropertyFormField.FURNISHING_TYPE to binding.tvFurnishing,
        PropertyFormField.IS_PET_FRIENDLY to binding.tvPetFriendly,
        PropertyFormField.PREFERRED_TENANT_TYPE to binding.tvTenantType,
        PropertyFormField.PREFERRED_BACHELOR_TYPE to binding.tvPreferredBachelor,
        PropertyFormField.BUILT_UP_AREA to binding.tilBuiltUpArea,
        PropertyFormField.AVAILABLE_FROM to binding.tilAvailableFrom,
        PropertyFormField.PRICE to binding.tilPrice,
        PropertyFormField.IS_MAINTENANCE_SEPARATE to binding.tvMaintenanceSeparate,
        PropertyFormField.MAINTENANCE_CHARGES to binding.tilMaintenanceCharge,
        PropertyFormField.SECURITY_DEPOSIT to binding.tilSecurityDeposit,
        PropertyFormField.IMAGES to binding.tvPropertyImages,
    ) }

    private fun requestFocus() {
        for((field, view) in fieldWithViewMap.entries) {
            if (viewModel.getFormErrorMap(field).value != null) {
                view.requestFocus()
                binding.formScrollView.smoothScrollTo(0, view.top)
                break
            }
        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showError(error: String = getString(R.string.please_resolve_all_the_errors_msg)) {
        binding.tvErrorMsg.apply {
            alpha = 1f
            visibility = View.VISIBLE
            text = error

            postDelayed({
                animate()
                    .alpha(0f)
                    .setDuration(750)
                    .withEndAction {
                        visibility = View.GONE
                    }
                },
                1500
            )
        }
    }

    private fun hideError() {

    }

    private fun setEditTextListeners() {
        formTextInputFieldInfoList.forEach{ textInputFieldInfo ->
            textInputFieldInfo.editText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val text = textInputFieldInfo.editText.text.toString().trim()
                    viewModel.updateFormValue(
                        textInputFieldInfo.field,
                        text
                    )
                }
            }

            if (textInputFieldInfo.field.isRequired)
                textInputFieldInfo.editText.addTextChangedListener {
                    viewModel.clearFormFieldError(textInputFieldInfo.field)
                }
        }
    }

    private fun setSingleSelectableChipGroupListeners() {
        fun updateValue(field: PropertyFormField, chipId: Int) = when(field) {
            PropertyFormField.BHK -> viewModel.updateFormValue(field, chipIdToBHK.getValue(chipId))
            PropertyFormField.TYPE -> viewModel.updateFormValue(field, chipIdToPropertyType.getValue(chipId))
            PropertyFormField.FURNISHING_TYPE -> {
                val furnishingType = chipIdToFurnishingType.getValue(chipId)
                viewModel.updateFormValue(field, furnishingType)
                // DECIDE TO SHOW ADD AMENITIES BUTTON
                binding.btnAddAmenities.visibility = if(furnishingType == FurnishingType.UN_FURNISHED)
                    View.GONE
                else
                    View.VISIBLE
            }
            PropertyFormField.IS_PET_FRIENDLY -> viewModel.updateFormValue(field, chipId == R.id.chipPetFriendlyYes)
            PropertyFormField.PREFERRED_BACHELOR_TYPE -> viewModel.updateFormValue(field, chipIdToBachelorType.getValue(chipId))
            PropertyFormField.IS_MAINTENANCE_SEPARATE -> viewModel.updateFormValue(field, chipId == R.id.chipSeparate)
            else -> logWarning("Invalid PropertyFormField for updateValueInViewModel")
        }

        formSingleSelectChipGroupsInfo.forEach {
            it.chipGroup.setOnCheckedStateChangeListener { chipGroup, checkedIds ->
                if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
                updateValue(it.field, checkedIds[0])
            }
        }

        binding.chipSeparate.setOnCheckedChangeListener { chip, _ ->
            onMaintenanceSeparateSelect(chip.isChecked)
        }
    }

    private fun showAnotherPhotoDialog() {
        AlertDialog.Builder(_context)
            .setMessage("Take another photo ?")
            .setPositiveButton("Yes") {_, _ ->
                imageUploadHelper.checkCameraPermissionAndOpenCamera(_context)
            }
            .setNegativeButton("Done", null)
            .show()
    }

    private fun showAddImageOptions() {
        val options = arrayOf("Camera", "Gallery")
        AlertDialog.Builder(_context)
            .setTitle("Add Image")
            .setItems(options) { _, which ->
                when(which) {
                    0 -> imageUploadHelper.checkCameraPermissionAndOpenCamera(_context)
                    1 -> imageUploadHelper.openImagePicker()
                }
            }
            .show()
    }

    private fun setListeners() {
        setEditTextListeners()
        setSingleSelectableChipGroupListeners()
        setCounterViewsListeners()
        setPreferredTenantListeners()

        // Other Listeners
        with(binding) {
            // Available From Date Picker
            tilAvailableFrom.setEndIconOnClickListener {
                myDatePicker.show(parentFragmentManager, "DATE_PICKER")
            }

            etAvailableFrom.addTextChangedListener {
                viewModel.updateFormValue(
                    PropertyFormField.AVAILABLE_FROM, etAvailableFrom.text.toString()
                )
            }

            // Open Amenities Sheet
            btnAddAmenities.setOnClickListener {
                amenitiesBottomSheet.show(parentFragmentManager, "AmenitiesBottomSheet")
            }

            // Images
            imgUploadImages.setOnClickListener { imageUploadHelper.openImagePicker() }
            btnUploadImages.setOnClickListener { imageUploadHelper.openImagePicker() }
            imgTakeImages.setOnClickListener {
                imageUploadHelper.checkCameraPermissionAndOpenCamera(_context)
            }
            btnTakeImages.setOnClickListener {
                imageUploadHelper.checkCameraPermissionAndOpenCamera(_context)
            }
            ibtnAddMoreImages.setOnClickListener {
                showAddImageOptions()
            }

            // Save Button
            btnSubmit.setOnClickListener {
                binding.root.clearFocus() // Clear Focus To Update Changes to ViewModel
                if (propertyIdToEdit == null)
                    viewModel.createProperty(currentUser.id)
                else {
                    if (!viewModel.isFormDirty()) {
                        _context.showToast("No changes are made to perform update.")
                        return@setOnClickListener
                    }

                    viewModel.updateProperty(currentUser.id)
                }
            }
        }
    }

    private fun setCounterViewsListeners() {
        with(binding) {
            // Parking, BathRoom
            formCounterViewList.forEach { (field, counterView) ->
                counterView.apply {
                    onCountIncrementListener = {
                        val newValue = counterView.count + 1
                        viewModel.updateFormValue(field , newValue.toString())
                    }
                    onCountDecrementListener = {
                        val newValue = counterView.count - 1
                        viewModel.updateFormValue(field , newValue.toString())
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

                if (R.id.chipFamily in checkedIds) tenants.add(TenantType.FAMILY)
                if (R.id.chipBachelors in checkedIds) tenants.add(TenantType.BACHELORS)

                viewModel.updatePreferredTenants(tenants)
            }

            chipBachelors.setOnCheckedChangeListener { chip, _ ->
                // Note: 2nd param isChecked is not giving correct data.
                onBachelorsSelectChange(chip.isChecked)
            }
        }
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

    private fun onBachelorsSelectChange(isSelected: Boolean) {
        with(binding) {
            if (isSelected) {
                hScrollOfPreferredBachelors.visibility = View.VISIBLE

                if (chipGroupBachelorPreference.checkedChipId == View.NO_ID) // If nothing is selected then only select Open For Both
                    chipOpenForBoth.isChecked = true
            } else {
                hScrollOfPreferredBachelors.visibility = View.GONE
                // Remove selected bachelors
                viewModel.updateFormValue(PropertyFormField.PREFERRED_BACHELOR_TYPE, null)
            }
        }
    }

    private fun onMaintenanceSeparateSelect(isSelected: Boolean) {
        binding.tilMaintenanceCharge.visibility = if (isSelected) View.VISIBLE else View.GONE
    }

    override fun onDetach() {
        super.onDetach()
        if (hideAndShowBottomNav)
            bottomNavController.showBottomNav()
    }

    private data class TextInputFieldInfo (
        val field: PropertyFormField,
        val inputLayout: TextInputLayout,
        val editText: TextInputEditText
    )

    private data class SingleSelectableChipGroupInfo (
        val field: PropertyFormField,
        val label: TextView,
        val chipGroup: ChipGroup,
    )
}