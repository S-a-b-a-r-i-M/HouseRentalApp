package com.example.houserentalapp.presentation.ui.property

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentCreatePropertyBinding
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Locale

class CreatePropertyFragment : Fragment() {

    private var _binding: FragmentCreatePropertyBinding? = null
    private val binding: FragmentCreatePropertyBinding
        get() = _binding!!

    private val myDatePicker by lazy {
        getDatePicker()
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

        // Enable Tool Bar
        setCustomToolBar()

        // OnClick Listeners
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        with(binding) {

            // Available From Date Picker
            etAvailableFrom.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
                    if (hasFocus)
                        myDatePicker.show(parentFragmentManager, "DATE_PICKER")
                }

            // ADD AMENITIES // TODO
            btnFurnishingDetails.setOnClickListener {
                val sheet = AmenitiesBottomSheet()
                sheet.show(parentFragmentManager, "AmenitiesBottomSheet")
            }

            // LOOKING TO
            val lookingToButtons = SingleSelectableGroupedButtons(
                listOf(
                    SelectableMaterialButtonData(btnRent, onSelect = ::onRentSelect),
                    SelectableMaterialButtonData(btnSell, onSelect = ::onSellSelect)
                )
            )
            btnRent.isChecked = true // As of now only supporting Rent
            btnSell.isClickable = false

            // PROPERTY TYPE
            val propertyButtons = SingleSelectableGroupedButtons(
                listOf(
                    SelectableMaterialButtonData(btnApartment),
                    SelectableMaterialButtonData(btnVilla),
                    SelectableMaterialButtonData(btnFormHouse),
                    SelectableMaterialButtonData(btnStudio),
                    SelectableMaterialButtonData(btnOther),
                )
            )

            // BHK
            val bhkButtons = SingleSelectableGroupedButtons(
                listOf(
                    SelectableMaterialButtonData(btn1BHK),
                    SelectableMaterialButtonData(btn2BHK),
                    SelectableMaterialButtonData(btn3BHK),
                    SelectableMaterialButtonData(btn4BHK),
                    SelectableMaterialButtonData(btn5AboveBHK),
                )
            )

            // FURNISHING TYPE
            val furnishingTypeButtons = SingleSelectableGroupedButtons(
                listOf(
                    SelectableMaterialButtonData(btnFullyFurnished),
                    SelectableMaterialButtonData(btnSemiFurnished),
                    SelectableMaterialButtonData(btnUnFurnished),
                )
            )

            // PET FRIENDLY
            val petFriendlyButtons = SingleSelectableGroupedButtons(
                listOf(
                    SelectableMaterialButtonData(btnPetFriendlyYes),
                    SelectableMaterialButtonData(btnPetFriendlyNo),
                )
            )

            // TENANT TYPE
            val tenantTypeButtons = SingleSelectableGroupedButtons(
                listOf(
                    SelectableMaterialButtonData(btnFamily),
                    SelectableMaterialButtonData(
                        btnBachelors,
                        onSelect = ::onBachelorsSelect,
                        onDeSelect = ::onBachelorsDeSelect
                    )
                )
            )

            // Maintenance Charge
            val maintenanceChargeButtons = SingleSelectableGroupedButtons(
                listOf(
                    SelectableMaterialButtonData(btnIncludeInRent),
                    SelectableMaterialButtonData(
                        btnSeparate,
                        onSelect = ::onMaintenanceSeparateSelect,
                        onDeSelect = ::onMaintenanceSeparateDeSelect
                    )
                )
            )
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

    private fun onRentSelect() {
        binding.tilBudget.hint = "Monthly Rent"
    }

    private fun onSellSelect() {
        binding.tilBudget.hint = "Budget"
    }

    private fun onBachelorsSelect() {
        logInfo("<------------ onBachelorsSelect ------------>")
        binding.preferredBachelorContainer.visibility = View.VISIBLE
        // Just Select Open for both by default.
        val bachelorsTypeButtons = SingleSelectableGroupedButtons(
            listOf(
                SelectableMaterialButtonData(binding.btnOpenForBoth),
                SelectableMaterialButtonData(binding.btnOnlyMen),
                SelectableMaterialButtonData(binding.btnOnlyWomen),
            )
        )

        binding.btnOpenForBoth.isChecked = true // Note: It won't invoke onclick of btnOpenForBoth
    }

    private fun onBachelorsDeSelect() {
        logInfo("<------------ onBachelorsDeSelect ------------>")
        binding.preferredBachelorContainer.visibility = View.GONE
    }

    private fun onMaintenanceSeparateSelect() {
        logInfo("<------------ onMaintenanceSeparateSelect ------------>")
        binding.tilMaintenanceCharge.visibility = View.VISIBLE
    }

    private fun onMaintenanceSeparateDeSelect() {
        logInfo("<------------ onMaintenanceSeparateDeSelect ------------>")
        binding.tilMaintenanceCharge.visibility = View.GONE
    }

    private fun setCustomToolBar() {
        with(binding) {
            titleTV.text = getString(R.string.create_property)

            backImgBtn.setOnClickListener {
                (requireActivity() as AppCompatActivity).onBackPressedDispatcher.onBackPressed()
            }
        }
    }
}