package com.example.houserentalapp.presentation.ui.property

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentCreatePropertyBinding
import com.example.houserentalapp.domain.model.ImageSource
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.model.enums.BHK
import com.example.houserentalapp.domain.model.enums.BachelorType
import com.example.houserentalapp.domain.model.enums.FurnishingType
import com.example.houserentalapp.domain.model.enums.PropertyType
import com.example.houserentalapp.domain.model.enums.TenantType
import com.example.houserentalapp.presentation.ui.MainActivity
import com.example.houserentalapp.presentation.ui.property.viewmodel.CreatePropertyViewModel
import com.example.houserentalapp.presentation.enums.PropertyFormField
import com.example.houserentalapp.presentation.ui.common.CounterView
import com.example.houserentalapp.presentation.ui.property.viewmodel.SharedDataViewModel
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.getValue

class CreatePropertyFragment : Fragment(R.layout.fragment_create_property) {
    private lateinit var binding: FragmentCreatePropertyBinding
    private lateinit var mainActivity: MainActivity
    private lateinit var currentUser: User

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

    private val imagesPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { it ->
        if (it.resultCode == RESULT_OK && it.data != null) {
            val clipData = it.data?.clipData
            val imageUris = mutableListOf<Uri>()

            if (clipData != null)
            // Multiple images selected
                for (i in 0 until clipData.itemCount)
                    imageUris.add(clipData.getItemAt(i).uri)
            else
            // Single image selected
                it.data?.data?.let { uri ->  imageUris.add(uri) }

            if (imageUris.isNotEmpty()) {
                viewModel.addPropertyImages(imageUris)
                requireActivity().showToast("${imageUris.size} selected successfully")
            }
        }
    }

    private lateinit var photoUri: Uri
    private val openCameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success){
            logInfo("got image $photoUri")
            viewModel.addPropertyImage(photoUri)
            showAnotherPhotoDialog()
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            logInfo("User approved the camera permission")
            openCamera()
        } else {
            logInfo("User denied the camera permission")
            mainActivity.showToast("Please provide camera permission to take pictures")
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        propertyIdToEdit = arguments?.getLong(PROPERTY_ID_KEY)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCreatePropertyBinding.bind(view)
        // Take Current User
        currentUser = sharedDataViewModel.currentUser ?: run {
            mainActivity.showToast("Login again...")
            mainActivity.finish()
            return
        }
        propertyIdToEdit?.let { viewModel.loadPropertyToEdit(it) } // If Edit Mode

        // Set up UI
        setupUI()

        // Set Listeners
        setListeners()

        // Observe View Model
        observeViewModel()

        // Handle Back Press
        addBackPressCallBack()
    }

    private fun addBackPressCallBack() {
        mainActivity.onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleOnBackNavigation()
                }
            })
    }

    private fun handleOnBackNavigation() {
        if (viewModel.isFormDirty())
            AlertDialog.Builder(mainActivity)
                .setTitle("Discard Changes")
                .setMessage("Are you sure you want to discard the form?")
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
        mainActivity.hideBottomNav()
        // Add paddingBottom to avoid system bar overlay
        setSystemBarBottomPadding(binding.root)

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
                        it.inputLayout.hint.toString(), mainActivity
                    )
            }

            formSingleSelectChipGroupsInfo.forEach {
                if (it.field.isRequired)
                    it.label.text = getRequiredStyleLabel(
                        it.label.text.toString(), mainActivity
                    )
            }

            tvTenantType.text = getRequiredStyleLabel(
                tvTenantType.text.toString(), mainActivity
            )

            tvPropertyImages.text = getRequiredStyleLabel(
                tvPropertyImages.text.toString(), mainActivity
            )
        }
    }

    private val chipIdToBHK = mapOf(
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

        viewModel.getFormErrorMap(PropertyFormField.IMAGES).observe(viewLifecycleOwner) {
            binding.tvPropertyImages.setTextColor(getErrorState(it))
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

    private fun observePropertyData() {
        with(binding) {
            viewModel.propertyDataUI.observe(viewLifecycleOwner) {
                updateTextInput(etPropertyName, it.name)
                updateTextInput(etPropertyDes, it.description)
                updateTextInput(etBuiltUpArea, it.builtUpArea)
                updateTextInput(etPrice, it.price)
                updateTextInput(etMaintenanceCharge, it.maintenanceCharges)
                updateTextInput(etSecurityDeposit, it.securityDepositAmount)
                updateTextInput(etAvailableFrom, it.availableFrom)
                updateTextInput(etStreet, it.street)
                updateTextInput(etLocality, it.locality)
                updateTextInput(etCity, it.city)

                updateCount(bathRoomCounter, it.bathRoomCount)
                updateCount(coveredParkingCounter, it.countOfCoveredParking)
                updateCount(openParkingCounter, it.countOfOpenParking)

                if (it.type != null)
                    chipGroupPropertyType.check(propertyTypeToChipId.getValue(it.type))
                if (it.bhk != null)
                    chipGroupBHK.check(bhkToChipId.getValue(it.bhk))
                if (it.furnishingType != null)
                    chipGroupFurnishing.check(furnishingTypeToChipId.getValue(it.furnishingType))
                if (it.preferredBachelorType != null)
                    chipGroupBachelorPreference.check(bachelorTypeToChipId.getValue(it.preferredBachelorType))

                if (it.isMaintenanceSeparate != null) {
                    if (it.isMaintenanceSeparate)
                        chipSeparate.isChecked = true
                    else
                        chipIncludeInRent.isChecked = true
                }
                if (it.isPetAllowed != null) {
                    if (it.isPetAllowed)
                        chipPetFriendlyYes.isChecked = true
                    else
                        chipPetFriendlyNo.isChecked = true
                }

                it.preferredTenantTypes?.forEach { tenant ->
                    chipGroupTenantType.check(tenantTypeToChipId.getValue(tenant))
                }
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
                        when(val imageSource = propertyImages[i].imageSource) {
                            is ImageSource.LocalFile -> {
                                val file = File(imageSource.filePath)
                                if (!file.exists()) {
                                    logWarning("Image(${imageSource.filePath}) is not exists")
                                    continue
                                }
                                imageView.setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))
                            }
                            is ImageSource.Uri -> {
                                imageView.setImageURI(imageSource.uri)
                            }
                        }

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
                            setTextColor(context.resources.getColor(R.color.primary_blue_dark))
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
                                ).apply { setMargins(5.dpToPx(context)) }
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
        observePropertyData()
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

                        val message = if (propertyIdToEdit == null)
                            "Property posted successfully"
                        else
                            "Property updated successfully"
                        mainActivity.showToast(message)
                        parentFragmentManager.popBackStack()
                    }
                    is ResultUI.Error -> {
                        hideProgressBar()
                        showError("Unexpected Error occurred, try later.")
                        mainActivity.showToast("Unexpected Error❌ occurred, please try later.")
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
                    clearValidationError()
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
            textInputFieldInfo.editText.addTextChangedListener {
                viewModel.updateFormValue(
                    textInputFieldInfo.field,
                    textInputFieldInfo.editText.text.toString()
                )
            }
        }
    }

    private fun setSingleSelectableChipGroupListeners() {
        fun updateValueInViewModel(field: PropertyFormField, chipId: Int) = when(field) {
            PropertyFormField.BHK -> viewModel.updateFormValue(field, chipIdToBHK.getValue(chipId))
            PropertyFormField.TYPE -> viewModel.updateFormValue(field, chipIdToPropertyType.getValue(chipId))
            PropertyFormField.FURNISHING_TYPE -> viewModel.updateFormValue(field, chipIdToFurnishingType.getValue(chipId))
            PropertyFormField.IS_PET_FRIENDLY -> viewModel.updateFormValue(field, chipId == R.id.chipPetFriendlyYes)
            PropertyFormField.PREFERRED_BACHELOR_TYPE -> viewModel.updateFormValue(field, chipIdToBachelorType.getValue(chipId))
            PropertyFormField.IS_MAINTENANCE_SEPARATE -> viewModel.updateFormValue(field, chipId == R.id.chipSeparate)
            else -> logWarning("Invalid PropertyFormField for updateValueInViewModel")
        }

        formSingleSelectChipGroupsInfo.forEach {
            it.chipGroup.setOnCheckedStateChangeListener { chipGroup, checkedIds ->
                if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
                updateValueInViewModel(it.field, checkedIds[0])
            }
        }

        binding.chipSeparate.setOnCheckedChangeListener { chip, _ ->
            onMaintenanceSeparateSelect(chip.isChecked)
        }
    }

    private fun openImagePicker() {
        // Without Permission also it's working
        // action will tell what exactly we are intent to do.
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) //
        }
        imagesPickerLauncher.launch(intent)
    }

    private fun checkCameraPermissionAndOpenCamera() {
        // Camera Permission
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        )
            permissionLauncher.launch(Manifest.permission.CAMERA)
        else
            openCamera()
    }

    private fun openCamera() {
        val photoFile = File.createTempFile("IMG_", ".jpg", mainActivity.cacheDir)
        photoUri = FileProvider.getUriForFile(
            mainActivity,
            mainActivity.packageName + ".provider",
            photoFile
        )
        openCameraLauncher.launch(photoUri)
    }

    private fun showAnotherPhotoDialog() {
        AlertDialog.Builder(mainActivity)
            .setMessage("Take another photo ?")
            .setPositiveButton("Yes") {_, _ -> checkCameraPermissionAndOpenCamera() }
            .setNegativeButton("Done") {_, _ -> }
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
            etAvailableFrom.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
                if (hasFocus) myDatePicker.show(parentFragmentManager, "DATE_PICKER")
            }

            // Open Amenities Sheet
            btnAddAmenities.setOnClickListener {
                amenitiesBottomSheet.show(parentFragmentManager, "AmenitiesBottomSheet")
            }

            // Images
            imgUploadImages.setOnClickListener { openImagePicker() }
            btnUploadImages.setOnClickListener { openImagePicker() }
            imgTakeImages.setOnClickListener { checkCameraPermissionAndOpenCamera() }
            btnTakeImages.setOnClickListener { checkCameraPermissionAndOpenCamera() }

            // Save Button
            btnSubmit.setOnClickListener {
                if (propertyIdToEdit == null)
                    viewModel.createProperty(currentUser.id)
                else {
                    if (!viewModel.isFormDirty()) {
                        mainActivity.showToast("No changes are made to perform update.")
                        return@setOnClickListener
                    }

                    viewModel.updateProperty(currentUser.id)
                }
            }
        }
    }

    private fun setCounterViewsListeners() {
        fun getCurrentValue(filed: PropertyFormField) = when (filed) {
            PropertyFormField.COVERED_PARKING_COUNT -> viewModel.propertyDataUI.value?.countOfCoveredParking
            PropertyFormField.OPEN_PARKING_COUNT -> viewModel.propertyDataUI.value?.countOfOpenParking
            PropertyFormField.BATH_ROOM_COUNT -> viewModel.propertyDataUI.value?.bathRoomCount
            else -> null
        }

        with(binding) {
            // Parking, BathRoom
            formCounterViewList.forEach { (field, counterView) ->
                counterView.apply {
                    onCountIncrementListener = {
                        val newValue = (getCurrentValue(field)?.toIntOrNull() ?: 0) + 1
                        viewModel.updateFormValue(field , newValue.toString())
                    }
                    onCountDecrementListener = {
                        val newValue = (getCurrentValue(field)?.toIntOrNull() ?: 0) - 1
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
        mainActivity.showBottomNav()
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

    companion object {
        const val PROPERTY_ID_KEY = "propertyId"
    }
}