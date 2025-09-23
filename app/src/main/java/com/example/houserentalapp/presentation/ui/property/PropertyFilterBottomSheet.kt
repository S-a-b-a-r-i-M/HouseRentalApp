package com.example.houserentalapp.presentation.ui.property

import android.icu.text.DecimalFormat
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentPropertyFilterBottomSheetBinding
import com.example.houserentalapp.domain.model.enums.BHK
import com.example.houserentalapp.domain.model.enums.FurnishingType
import com.example.houserentalapp.domain.model.enums.PropertyType
import com.example.houserentalapp.domain.model.enums.TenantType
import com.example.houserentalapp.presentation.ui.property.viewmodel.FiltersViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.slider.RangeSlider

class PropertyFilterBottomSheet : BottomSheetDialogFragment(
    R.layout.fragment_property_filter_bottom_sheet
) {
    private lateinit var binding: FragmentPropertyFilterBottomSheetBinding
    private val filtersViewModel: FiltersViewModel by activityViewModels()

    private val chipIdToBHK = mapOf(
        R.id.chip1RK to BHK.ONE_RK,
        R.id.chip1BHK to BHK.ONE_BHK,
        R.id.chip2BHK to BHK.TWO_BHK,
        R.id.chip3BHK to BHK.THREE_BHK,
        R.id.chip4BHK to BHK.FOUR_BHK,
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
    private val propertyTypeToChipId = chipIdToPropertyType.entries.associate { (k, v)-> v to k }
    private val chipIdToFurnishingType = mapOf(
        R.id.chipFurnished to FurnishingType.FULLY_FURNISHED,
        R.id.chipUnFurnished to FurnishingType.UN_FURNISHED,
        R.id.chipSemiFurnished to FurnishingType.SEMI_FURNISHED,
    )
    // TODO: This map is not needed
    private val furnishingTypeToChipId = chipIdToFurnishingType.entries.associate {(k, v) -> v to k}
    private val chipIdToTenantType = mapOf(
        R.id.chipFamily to TenantType.FAMILY,
        R.id.chipBachelor to TenantType.BACHELORS
    )
    // TODO: This map is not needed
    private val tenantTypeToChipId = chipIdToTenantType.entries.associate { (k, v) -> v to k }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPropertyFilterBottomSheetBinding.bind(view)

        setupUI()
        setupListeners()
        setupObservers()
    }

    private fun setupUI() {
        with(binding) {
            tvMinBudget.text = formatAmount(MIN_PRICE)
            tvMaxBudget.text = formatAmount(MAX_PRICE)
            rSliderBudget.apply {
                valueFrom = MIN_PRICE
                valueTo = MAX_PRICE
                setValues(valueFrom, valueTo)
            }
        }
    }

    private fun setupBudgetSliderListeners() {
        with(binding) {
            // Real-time value updates while sliding
            rSliderBudget.addOnChangeListener { slider, value, fromUser ->
                if (fromUser) {
                    val (min, max) = slider.values
                    if (value == min)
                        tvMinBudget.text = formatAmount(min)
                    else
                        tvMaxBudget.text = formatAmount(max)
                }
            }

            // Final value when user stops sliding
            rSliderBudget.addOnSliderTouchListener( object : RangeSlider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: RangeSlider) { }

                override fun onStopTrackingTouch(slider: RangeSlider) {
                    filtersViewModel.setBudget(
                        Pair(slider.values[0], slider.values[1])
                    )
                }
            })
        }
    }

    private fun applyChipGroupSelections() {
        with(binding) {
            val selectedBHKList = chipGroupBHK.checkedChipIds.map { chipIdToBHK.getValue(it) }
            val selectedProperties = chipGroupProperties.checkedChipIds.map {
                chipIdToPropertyType.getValue(it)
            }
            val selectedFurnishings = chipGroupFurnishings.checkedChipIds.map {
                chipIdToFurnishingType.getValue(it)
            }
            val selectedTenants = chipGroupTenants.checkedChipIds.map {
                chipIdToTenantType.getValue(it)
            }

            filtersViewModel.setPropertyFilters(filtersViewModel.filters.value!!.copy(
                bhkTypes = selectedBHKList,
                propertyTypes = selectedProperties,
                furnishingTypes = selectedFurnishings,
                tenantTypes = selectedTenants,
            ))
        }
    }

    private fun setupListeners() {
        setupBudgetSliderListeners()

        with(binding) {
            btnClose.setOnClickListener {
                dismiss() // Close The BottomSheet
            }

            btnApplyFilters.setOnClickListener {
                applyChipGroupSelections()
                filtersViewModel.triggerApplyFilters()
                dismiss()
            }
        }
    }

    private fun formatAmount(amount: Float): String {
        val df = DecimalFormat("#.##")

        return when {
            amount >= 1_00_000 ->  "%.2f L".format(amount / 1_00_000.0)
            amount >= 1000 -> "%.1f K".format(amount / 1000.0)
            else -> "$amount"
        }

//        return when {
//            amount >= 1_00_000 -> df.format(amount / 1_00_000.0) +" L"
//            amount >= 1000 -> df.format(amount / 1000.0) +" K"
//            else -> "$amount"
//        }
    }

    private fun setupObservers() {
        filtersViewModel.filters.observe(viewLifecycleOwner) { propertyFilters ->
            propertyFilters.bhkTypes.forEach { bhk ->
                binding.root.findViewById<Chip>(bhkToChipId.getValue(bhk)).isChecked = true
            }

            propertyFilters.propertyTypes.forEach { propertyType ->
                binding.root.findViewById<Chip>(
                    propertyTypeToChipId.getValue(propertyType)
                ).isChecked = true
            }

            propertyFilters.furnishingTypes.forEach { furnishingType ->
                binding.root.findViewById<Chip>(
                    furnishingTypeToChipId.getValue(furnishingType)
                ).isChecked = true
            }

            propertyFilters.tenantTypes.forEach { tenantType ->
                binding.root.findViewById<Chip>(
                    tenantTypeToChipId.getValue(tenantType)
                ).isChecked = true
            }

            propertyFilters.budget?.let { (min , max) ->
                binding.rSliderBudget.apply {
                    binding.tvMinBudget.text = formatAmount(min)
                    binding.tvMaxBudget.text = formatAmount(max)
                    setValues(min, max)
                }
            }
        }
    }

    companion object {
        private const val MIN_PRICE = 1000f
        private const val MAX_PRICE = 2_00_000f
    }
}