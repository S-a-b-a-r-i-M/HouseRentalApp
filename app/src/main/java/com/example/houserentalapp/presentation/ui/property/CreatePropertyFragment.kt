package com.example.houserentalapp.presentation.ui.property

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import com.example.houserentalapp.R

import com.example.houserentalapp.databinding.FragmentCreatePropertyBinding
import com.example.houserentalapp.domain.model.enums.TenantType
import com.example.houserentalapp.presentation.ui.property.viewmodel.CreatePropertyViewModel
import com.example.houserentalapp.presentation.ui.property.viewmodel.PropertyFormField
import com.example.houserentalapp.presentation.utils.extensions.createPropertyViewModelFactory
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.showToast
import com.example.houserentalapp.presentation.utils.helpers.setSystemBarBottomPadding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Locale

class CreatePropertyFragment : Fragment() {

    private var _binding: FragmentCreatePropertyBinding? = null
    private val binding: FragmentCreatePropertyBinding
        get() = _binding!!
    private val amenitiesBottomSheet by lazy { AmenitiesBottomSheet() }

    private val viewModel: CreatePropertyViewModel by activityViewModels {
        createPropertyViewModelFactory()
    }

    private lateinit var sgbPropertyType: SingleSelectableGroupedButtons
    private lateinit var sgbLookingTo: SingleSelectableGroupedButtons
    private lateinit var sgbBhk: SingleSelectableGroupedButtons
    private lateinit var sgbFurnishingType: SingleSelectableGroupedButtons
    private lateinit var sgbPetFriendly: SingleSelectableGroupedButtons
    private lateinit var sgbBachelorType: SingleSelectableGroupedButtons
    private lateinit var sgbIsMaintenanceSeparate: SingleSelectableGroupedButtons

    private val myDatePicker by lazy { getDatePicker() }

    val imagesPickerLauncher = registerForActivityResult(
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCreatePropertyBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Add paddingBottom to avoid system bar overlay
        setSystemBarBottomPadding(binding.root)

        // Set up UI
        setupUI()

        // Listeners
        setListeners()

        // Observe View Model
        observeViewModel()

        /* TODO
         * 1. Image Upload (Click From Camera , Upload From Gallery)
         */
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
            observeError(PropertyFormField.NAME) { tilPropertyName.error = it }
            observeValue(PropertyFormField.NAME, etPropertyName)
            observeError(PropertyFormField.CITY) { tilCity.error = it }
            observeValue(PropertyFormField.CITY, etCity)
            observeError(PropertyFormField.LOCALITY) { tilLocality.error = it }
            observeValue(PropertyFormField.LOCALITY, etLocality)
            observeError(PropertyFormField.BUILT_UP_AREA) { tilBuiltUpArea.error = it }
            observeValue(PropertyFormField.BUILT_UP_AREA, etBuiltUpArea)
            observeError(PropertyFormField.AVAILABLE_FROM) { tilAvailableFrom.error = it }
            observeValue(PropertyFormField.AVAILABLE_FROM, etAvailableFrom)
            observeError(PropertyFormField.PRICE) { tilPrice.error = it }
            observeValue(PropertyFormField.PRICE, etPrice)
            observeError(PropertyFormField.MAINTENANCE_CHARGES) { tilMaintenanceCharge.error = it }
            observeValue(PropertyFormField.MAINTENANCE_CHARGES, etMaintenanceCharge)
            observeError(PropertyFormField.SECURITY_DEPOSIT) { tilSecurityDeposit.error = it }
            observeValue(PropertyFormField.SECURITY_DEPOSIT, etSecurityDeposit)
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
            observeError(PropertyFormField.LOOKING_TO) {
                tvLookingTo.setTextColor(getErrorState(it))
                tvLookingToErr.visibility = if (it == null) View.GONE else View.VISIBLE
            }
            observeError(PropertyFormField.TYPE) {
                tvPropertyType.setTextColor(getErrorState(it))
            }
            observeError(PropertyFormField.BHK) {
                tvBHK.setTextColor(getErrorState(it))
            }
            observeError(PropertyFormField.FURNISHING_TYPE) {
                tvFurnishing.setTextColor(getErrorState(it))
            }
            observeError(PropertyFormField.IS_PET_FRIENDLY) {
                tvPetFriendly.setTextColor(getErrorState(it))
            }
            observeError(PropertyFormField.PREFERRED_TENANT_TYPE) {
                tvTenantType.setTextColor(getErrorState(it))
            }
            observeError(PropertyFormField.PREFERRED_BACHELOR_TYPE) {
                tvPreferredBachelor.setTextColor(getErrorState(it))
            }
            observeError(PropertyFormField.IS_MAINTENANCE_SEPARATE) {
                tvMaintenanceSeparate.setTextColor(getErrorState(it))
            }
        }
    }

    private fun observeViewModel() {
        observeEditTextFields()
        observeGroupedButtonFields()

        with(binding) {
            // Parking
            viewModel.getFormDataMap(PropertyFormField.COVERED_PARKING_COUNT)
                .observe(viewLifecycleOwner) { count ->
                    coveredParkingContainer.count = count?.toIntOrNull() ?: 0
                }

            viewModel.getFormDataMap(PropertyFormField.OPEN_PARKING_COUNT)
                .observe(viewLifecycleOwner) { count ->
                    openParkingContainer.count = count?.toIntOrNull() ?: 0
                }
        }
    }

    private fun setupUI() {
        setupCustomToolBar()
        setupSingleSelectableGroupedButtons()
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
            sgbLookingTo = SingleSelectableGroupedButtons(
                listOf(
                    SelectableMaterialButtonData(btnRent, ::onRentSelectChange),
                    SelectableMaterialButtonData(btnSell)
                )
            )

            // PROPERTY TYPE
            sgbPropertyType = SingleSelectableGroupedButtons(
                listOf(
                    SelectableMaterialButtonData(btnApartment),
                    SelectableMaterialButtonData(btnVilla),
                    SelectableMaterialButtonData(btnIndependentHouse),
                    SelectableMaterialButtonData(btnFormHouse),
                    SelectableMaterialButtonData(btnStudio),
                    SelectableMaterialButtonData(btnOther),
                )
            )

            // BHK
            sgbBhk = SingleSelectableGroupedButtons(
                listOf(
                    SelectableMaterialButtonData(btn1BHK),
                    SelectableMaterialButtonData(btn2BHK),
                    SelectableMaterialButtonData(btn3BHK),
                    SelectableMaterialButtonData(btn4BHK),
                    SelectableMaterialButtonData(btn5AboveBHK),
                )
            )

            // FURNISHING TYPE
            sgbFurnishingType = SingleSelectableGroupedButtons(
                listOf(
                    SelectableMaterialButtonData(btnFullyFurnished),
                    SelectableMaterialButtonData(btnSemiFurnished),
                    SelectableMaterialButtonData(btnUnFurnished),
                )
            )

            // PET FRIENDLY
            sgbPetFriendly = SingleSelectableGroupedButtons(
                listOf(
                    SelectableMaterialButtonData(btnPetFriendlyYes),
                    SelectableMaterialButtonData(btnPetFriendlyNo),
                )
            )

            // PREFERRED BACHELOR TYPE
            sgbBachelorType = SingleSelectableGroupedButtons(
                listOf(
                    SelectableMaterialButtonData(btnOpenForBoth),
                    SelectableMaterialButtonData(btnOnlyMen),
                    SelectableMaterialButtonData(btnOnlyWomen)
                )
            )

            // Maintenance Charge
            sgbIsMaintenanceSeparate = SingleSelectableGroupedButtons(
                listOf(
                    SelectableMaterialButtonData(btnIncludeInRent),
                    SelectableMaterialButtonData(
                        btnSeparate,
                        onSelectChange = ::onMaintenanceSeparateSelect,
                    )
                )
            )
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

            onStartTyping(etPropertyName, PropertyFormField.NAME)
            onStartTyping(etCity, PropertyFormField.CITY)
            onStartTyping(etLocality, PropertyFormField.LOCALITY)
            onStartTyping(etBuiltUpArea, PropertyFormField.BUILT_UP_AREA)
            onStartTyping(etAvailableFrom, PropertyFormField.AVAILABLE_FROM)
            onStartTyping(etPrice, PropertyFormField.PRICE)
            onStartTyping(etMaintenanceCharge, PropertyFormField.MAINTENANCE_CHARGES)
            onStartTyping(etSecurityDeposit, PropertyFormField.SECURITY_DEPOSIT)
        }
    }

    /*
    private fun updateEditTextChangesIntoViewModel() {
        with(binding) {
            viewModel.onFormValueChanged(PropertyFormField.NAME, etPropertyName.text.toString())
            viewModel.onFormValueChanged(PropertyFormField.CITY, etCity.text.toString())
            viewModel.onFormValueChanged(PropertyFormField.LOCALITY, etLocality.text.toString())
            viewModel.onFormValueChanged(
                PropertyFormField.BUILT_UP_AREA,
                etBuiltUpArea.text.toString()
            )
            viewModel.onFormValueChanged(
                PropertyFormField.AVAILABLE_FROM,
                etAvailableFrom.text.toString()
            )
            viewModel.onFormValueChanged(PropertyFormField.PRICE, etPrice.text.toString())
            viewModel.onFormValueChanged(
                PropertyFormField.MAINTENANCE_CHARGES,
                etMaintenanceCharge.text.toString()
            )
            viewModel.onFormValueChanged(
                PropertyFormField.SECURITY_DEPOSIT,
                etSecurityDeposit.text.toString()
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // onSaveInstanceState is NOT called when:
        /*
        - User presses back button
            - App crashes
            - User switches to another app (sometimes)
        - Fragment gets replaced
        - User kills app from recent apps
         */
//        updateEditTextChangesIntoViewModel()
        // Can i save this into Bundle ?
        super.onSaveInstanceState(outState)
    }
     */

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
            setOnOptionSelectedListener(sgbLookingTo, PropertyFormField.LOOKING_TO)
            setOnOptionSelectedListener(sgbPropertyType, PropertyFormField.TYPE)
            setOnOptionSelectedListener(sgbBhk, PropertyFormField.BHK)
            setOnOptionSelectedListener(sgbFurnishingType, PropertyFormField.FURNISHING_TYPE)
            setOnOptionSelectedListener(sgbBachelorType, PropertyFormField.PREFERRED_BACHELOR_TYPE)
            setOnOptionSelectedListener(sgbIsMaintenanceSeparate, PropertyFormField.IS_MAINTENANCE_SEPARATE)
            setOnOptionSelectedListener(sgbPetFriendly, PropertyFormField.IS_PET_FRIENDLY)

            btnRent.performClick() // As of now only supporting Rent
            btnSell.apply {
                isClickable = false
                btnSell.alpha = 0.5f
            }
        }
    }

    private fun setListeners() {
        setEditTextListeners()
        setSelectableGroupedButtonListeners()
        setParkingListeners()
        setPreferredTenantListeners()

        // Other Listeners
        with(binding) {
            // Available From Date Picker
            etAvailableFrom.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
                if (hasFocus) myDatePicker.show(parentFragmentManager, "DATE_PICKER")
            }

            // Open Amenities Sheet
            btnFurnishingDetails.setOnClickListener {
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
            btnSubmit.setOnClickListener {
                viewModel.createProperty()
            }
        }
    }

    private fun setParkingListeners() {
        with(binding) {
            // Parking
            coveredParkingContainer.apply {
                onCountIncrementListener = {
                    viewModel.updateFormValue(
                        PropertyFormField.COVERED_PARKING_COUNT , 1
                    )
                }
                onCountDecrementListener = {
                    viewModel.updateFormValue(
                        PropertyFormField.COVERED_PARKING_COUNT , -1
                    )
                }
            }

            openParkingContainer.apply {
                onCountIncrementListener = {
                    viewModel.updateFormValue(
                        PropertyFormField.OPEN_PARKING_COUNT , 1
                    )
                }
                onCountDecrementListener = {
                    viewModel.updateFormValue(
                        PropertyFormField.OPEN_PARKING_COUNT , -1
                    )
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
            .setValidator(DateValidatorPointForward.from(System.currentTimeMillis()))

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
        binding.tilPrice.hint = if (isSelected) "₹ Monthly Rent" else "Budget"
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
}