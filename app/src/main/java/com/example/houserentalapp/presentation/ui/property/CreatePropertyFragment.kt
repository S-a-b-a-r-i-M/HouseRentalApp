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
import com.example.houserentalapp.domain.model.enums.TenantType
import com.example.houserentalapp.presentation.ui.MainActivity
import com.example.houserentalapp.presentation.ui.property.viewmodel.CreatePropertyViewModel
import com.example.houserentalapp.presentation.enums.PropertyFormField
import com.example.houserentalapp.presentation.ui.reusable.CounterView
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.createPropertyViewModelFactory
import com.example.houserentalapp.presentation.utils.extensions.dpToPx
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.showToast
import com.example.houserentalapp.presentation.utils.helpers.getRequiredStyleLabel
import com.example.houserentalapp.presentation.utils.helpers.setSystemBarBottomPadding
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Locale

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
    private val formSGButtonsInfoList = mutableListOf<SelectableGroupedButtonsInfo>()

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

    private fun observeEditTextFields() {
        // Helper function to observe
        fun observeError(field: PropertyFormField, observer: (String?) -> Unit) {
            // Observer Error
            viewModel.getFormErrorMap(field).observe(viewLifecycleOwner) {
                observer(it)
                logDebug("observeEditTextFields -> observeError -> $field: $it")
            }
        }

        fun observeValue(field: PropertyFormField, et: TextInputEditText) {
            // Observe Value
            viewModel.getFormDataMap(field).observe(viewLifecycleOwner) {
                if (it != null && et.text.toString() != it) {
                    et.setText(it)
                    logDebug("observeEditTextFields -> observeValue -> $field: $it")
                }
            }
        }

        with(binding) {
            formTextInputFieldInfoList.forEach {
                observeValue(it.field, it.editText)
                if (it.isRequired)
                    observeError(it.field) { error -> it.inputLayout.error = error }
            }
        }
    }

    private fun observeGroupedButtonFields() {
        // Helper function to observe Err
        fun observeError(field: PropertyFormField, onChange: (String?) -> Unit) {
            viewModel.getFormErrorMap(field).observe(viewLifecycleOwner) {
                onChange(it)
                logDebug("observeGroupedButtonFields -> observeError -> $field: $it")
            }
        }

        fun getErrorState(err: String?) = resources.getColor(
            if (err != null) R.color.red_error else R.color.gray_dark
        )

        with(binding) {
            formSGButtonsInfoList.forEach {
                observeError(it.field) { error ->
                    it.label.setTextColor(getErrorState(error))
                }
            }

            observeError(PropertyFormField.PREFERRED_TENANT_TYPE) {
                tvTenantType.setTextColor(getErrorState(it))
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
        observeEditTextFields()
        observeGroupedButtonFields()
        observeImageUri()

        with(binding) {
            // Counter Views
            formCounterViewList.forEach { (field, counterView) ->
                viewModel.getFormDataMap(field).observe(viewLifecycleOwner) { count ->
                    counterView.count = count?.toIntOrNull() ?: 0
                }
            }

            /*
            viewModel.getFormDataMap(PropertyFormField.COVERED_PARKING_COUNT)
                .observe(viewLifecycleOwner) { count ->
                    coveredParkingCounter.count = count?.toIntOrNull() ?: 0
                }

            viewModel.getFormDataMap(PropertyFormField.OPEN_PARKING_COUNT)
                .observe(viewLifecycleOwner) { count ->
                    openParkingCounter.count = count?.toIntOrNull() ?: 0
                }

            // Bathroom
            viewModel.getFormDataMap(PropertyFormField.BATH_ROOM_COUNT)
                .observe(viewLifecycleOwner) { count ->
                    bathRoomCounter.count = count?.toIntOrNull() ?: 0
                }
             */

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
        // Hide Bottom nav.
        mainActivity.hideBottomNav()
        // Add paddingBottom to avoid system bar overlay
        setSystemBarBottomPadding(binding.root)

        setupCustomToolBar()
        setupSingleSelectableGroupedButtons()
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

            formSGButtonsInfoList.forEach {
                if (it.isRequired)
                    it.label.text = getRequiredStyleLabel(
                        it.label.text.toString(), mainActivity
                    )
            }
        }
    }

    private fun setupCustomToolBar() {
        with(binding) {
            titleTV.text = getString(R.string.create_property)

            backImgBtn.root.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun setupSingleSelectableGroupedButtons() {
        with(binding) {
            // LOOKING TO
            val sgbLookingTo = SingleSelectableGroupedButtons(
                listOf(
                    SelectableButtonData(btnRent, ::onRentSelectChange),
                    SelectableButtonData(btnSell)
                )
            )

            // PROPERTY TYPE
            val sgbPropertyType = SingleSelectableGroupedButtons(
                listOf(
                    SelectableButtonData(btnApartment),
                    SelectableButtonData(btnVilla),
                    SelectableButtonData(btnIndependentHouse),
                    SelectableButtonData(btnFormHouse),
                    SelectableButtonData(btnStudio),
                    SelectableButtonData(btnOther),
                )
            )

            // BHK
            val sgbBhk = SingleSelectableGroupedButtons(
                listOf(
                    SelectableButtonData(btn1BHK),
                    SelectableButtonData(btn2BHK),
                    SelectableButtonData(btn3BHK),
                    SelectableButtonData(btn4BHK),
                    SelectableButtonData(btn5AboveBHK),
                )
            )

            // FURNISHING TYPE
            val sgbFurnishingType = SingleSelectableGroupedButtons(
                listOf(
                    SelectableButtonData(btnFullyFurnished),
                    SelectableButtonData(btnSemiFurnished),
                    SelectableButtonData(btnUnFurnished),
                )
            )

            // PET FRIENDLY
            val sgbPetFriendly = SingleSelectableGroupedButtons(
                listOf(
                    SelectableButtonData(btnPetFriendlyYes),
                    SelectableButtonData(btnPetFriendlyNo),
                )
            )

            // PREFERRED BACHELOR TYPE
            val sgbBachelorType = SingleSelectableGroupedButtons(
                listOf(
                    SelectableButtonData(btnOpenForBoth),
                    SelectableButtonData(btnOnlyMen),
                    SelectableButtonData(btnOnlyWomen)
                )
            )

            // Maintenance Charge
            val sgbIsMaintenanceSeparate = SingleSelectableGroupedButtons(
                listOf(
                    SelectableButtonData(btnIncludeInRent),
                    SelectableButtonData(
                        btnSeparate,
                        ::onMaintenanceSeparateSelect,
                    )
                )
            )

            formSGButtonsInfoList.apply {
                add(SelectableGroupedButtonsInfo(
                    PropertyFormField.LOOKING_TO, tvLookingTo, sgbLookingTo
                ))
                add(SelectableGroupedButtonsInfo(
                    PropertyFormField.TYPE, tvPropertyType, sgbPropertyType
                ))
                add(SelectableGroupedButtonsInfo(
                    PropertyFormField.BHK, tvBHK, sgbBhk
                ))
                add(SelectableGroupedButtonsInfo(
                    PropertyFormField.FURNISHING_TYPE, tvFurnishing, sgbFurnishingType
                ))
                add(SelectableGroupedButtonsInfo(
                    PropertyFormField.IS_PET_FRIENDLY, tvPetFriendly, sgbPetFriendly
                ))
                add(SelectableGroupedButtonsInfo(
                    PropertyFormField.PREFERRED_BACHELOR_TYPE, tvPreferredBachelor, sgbBachelorType
                ))
                add(SelectableGroupedButtonsInfo(
                    PropertyFormField.IS_MAINTENANCE_SEPARATE, tvMaintenanceSeparate, sgbIsMaintenanceSeparate
                ))
            }
        }
    }

    private fun setEditTextListeners() {
        with(binding) {
            // ALL EDIT TEXT FIELDS
            fun onStartTyping(et: TextInputEditText, field: PropertyFormField) {
                et.addTextChangedListener {
                    viewModel.updateFormValue(field, et.text.toString())
                }
            }

            formTextInputFieldInfoList.forEach{
                onStartTyping(it.editText, it.field)
            }
        }
    }

    private fun setSelectableGroupedButtonListeners() {
        fun setOnOptionSelectedListener(
            selectableGroupedButtons: SingleSelectableGroupedButtons,
            field: PropertyFormField
        ) {
            selectableGroupedButtons.setOnOptionSelectedListener { buttonData ->
                logDebug("${buttonData.button.text} Selected in $field Group")
                // Button text is same as enum prop readable
                viewModel.updateFormValue(field, buttonData.button.text.toString())
            }
        }

        with(binding) {
            formSGButtonsInfoList.forEach {
                setOnOptionSelectedListener(it.groupedButtons, it.field)
            }

            btnRent.performClick() // As of now only supporting Rent
            btnSell.apply {
                isClickable = false
                alpha = 0.5f
            }
        }
    }

    private fun setListeners() {
        setEditTextListeners()
        setSelectableGroupedButtonListeners()
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
        with(binding) {
            // Parking, BathRoom
            formCounterViewList.forEach { (field, counterView) ->
                counterView.apply {
                    onCountIncrementListener = {
                        val newValue = (viewModel.getFormDataMap(field).value?.toIntOrNull() ?: 0) + 1
                        viewModel.updateFormValue(field , newValue.toString())
                    }
                    onCountDecrementListener = {
                        val newValue = (viewModel.getFormDataMap(field).value?.toIntOrNull() ?: 0) - 1
                        viewModel.updateFormValue(field , newValue.toString())
                    }
                }
            }
        }
    }

    private fun setPreferredTenantListeners() {
        with(binding) {
            // Handling Preferred Tenant Selection
            val preferredTenants = mutableSetOf<String>()
            fun onChangePreferredTenants(value: String, isAdded: Boolean) {
                if (isAdded)
                    preferredTenants.add(value)
                else
                    preferredTenants.remove(value)

                viewModel.updateFormValue(
                    PropertyFormField.PREFERRED_TENANT_TYPE,
                    preferredTenants.joinToString(",")
                )
            }

            btnFamily.addOnCheckedChangeListener { _, isChecked ->
                onChangePreferredTenants(TenantType.FAMILY.readable, isChecked)
            }

            btnBachelors.addOnCheckedChangeListener { _, isChecked ->
                onChangePreferredTenants(TenantType.BACHELORS.readable, isChecked)
                onBachelorsSelectChange(isChecked)
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

    private fun onRentSelectChange(isSelected: Boolean) {
        val text = if (isSelected) "₹ Monthly Rent" else "Budget"
        binding.tilPrice.hint = getRequiredStyleLabel(text, mainActivity)
    }

    private fun onBachelorsSelectChange(isSelected: Boolean) {
        logInfo("<------------ onBachelorsSelectChange: $isSelected ------------>")
        if (isSelected) {
            binding.preferredBachelorContainer.visibility = View.VISIBLE
            binding.btnOpenForBoth.isChecked = true // Note: It won't invoke onclick of btnOpenForBoth
        } else
            binding.preferredBachelorContainer.visibility = View.GONE
    }

    private fun onMaintenanceSeparateSelect(isSelected: Boolean) {
        logInfo("<------------ onMaintenanceSeparateSelect: $isSelected ------------>")
        binding.tilMaintenanceCharge.visibility = if (isSelected) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
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

    private data class SelectableGroupedButtonsInfo (
        val field: PropertyFormField,
        val label: TextView,
        val groupedButtons: SingleSelectableGroupedButtons,
        val isRequired: Boolean = true
    )
}