package com.example.houserentalapp.presentation.ui.property

import android.graphics.Typeface
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.fragment.app.activityViewModels
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentAmenitiesBottomSheetBinding
import com.example.houserentalapp.domain.model.enums.CountableInternalAmenity
import com.example.houserentalapp.domain.model.enums.InternalAmenity
import com.example.houserentalapp.domain.model.enums.SocialAmenity
import com.example.houserentalapp.presentation.ui.property.viewmodel.CreatePropertyViewModel
import com.example.houserentalapp.presentation.ui.common.CounterView
import com.example.houserentalapp.presentation.utils.extensions.createPropertyViewModelFactory
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.google.android.material.checkbox.MaterialCheckBox

/* TODO
    1. FIX: Countable Amenities color
    2. Add: Icons
 */
class AmenitiesBottomSheet : BottomSheetDialogFragment() {
    private var _binding: FragmentAmenitiesBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreatePropertyViewModel by activityViewModels {
        createPropertyViewModelFactory()
    }
    private val countableIntAmenitiesViewMap = mutableMapOf<CountableInternalAmenity, CounterView>()
    private val internalAmenitiesViewMap = mutableMapOf<InternalAmenity, MaterialCheckBox>()
    private val socialAmenitiesViewMap = mutableMapOf<SocialAmenity, MaterialCheckBox>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAmenitiesBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupUI()
        setupListener()
        setupObserver()
    }

    // Layout Params
    private val matchParentWrapContentParams = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    private val wrapContentParams = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    private val weightedWrapContentParams = LinearLayout.LayoutParams(
        0,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        1f // weight = 1
    )
    private fun buildFlatFurnishingsFields() {
        val context = requireActivity()
        // ADD TITLE
        val tvFlatFurnishings = TextView(context).apply {
            setTextAppearance(R.style.TextAppearance_App_Headline2)
            setText(R.string.flat_furnishings)
            setPadding(0, 10, 0, 20)
            setLayoutParams(matchParentWrapContentParams)
        }
        binding.amenitiesContainer.addView(tvFlatFurnishings)

        // ITERATE FROM CountableFlatFurnishings
        CountableInternalAmenity.entries.forEach {
            val counterView = CounterView(context).apply {
                label = it.readable
                labelSize = 14f
                labelColor = resources.getColor(R.color.black)
                labelStyle = Typeface.DEFAULT
                setPadding(30)
                setIconDimensions(30f, 30f)
                setLayoutParams(matchParentWrapContentParams)
            }
            binding.amenitiesContainer.addView(counterView)
            countableIntAmenitiesViewMap.put(it, counterView) // Add to map
        }

        // ITERATE FROM InternalFlatFurnishingsEnum
        InternalAmenity.entries.forEach { amenity ->
            val chackBox = addAmenityWithCheckBox(amenity.readable)
            internalAmenitiesViewMap.put(amenity, chackBox)
        }
    }

    private fun buildSocialAmenitiesFields() {
        // ADD TITLE
        val tvSocietyAmenities = TextView(context).apply {
            setTextAppearance(R.style.TextAppearance_App_Headline2)
            setText(R.string.society_amenities)
            setPadding(0, 20, 0, 20)
            setLayoutParams(matchParentWrapContentParams)
        }
        binding.amenitiesContainer.addView(tvSocietyAmenities)

        // ITERATE SocialAmenitiesEnum
        SocialAmenity.entries.forEach { amenity ->
            val checkBox = addAmenityWithCheckBox(amenity.readable)
            socialAmenitiesViewMap.put(amenity, checkBox)
        }
    }

    private fun setupUI() {
        buildFlatFurnishingsFields()
        buildSocialAmenitiesFields()
    }

    private fun setupListener() {
        binding.btnClose.setOnClickListener {
            dismiss() // Close The BottomSheet
        }

        // Internal Countable Amenities
        countableIntAmenitiesViewMap.forEach { (amenity, counterView) ->
            counterView.onCountIncrementListener = {
                viewModel.updateInternalCountableAmenity(amenity, 1)
            }
            counterView.onCountDecrementListener = {
                viewModel.updateInternalCountableAmenity(amenity, -1)
            }
        }

        // Internal Amenities
        internalAmenitiesViewMap.forEach { (amenity, checkBox) ->
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                viewModel.onInternalAmenityChanged(amenity, isChecked)
            }
        }

        // Social Amenities
        socialAmenitiesViewMap.forEach { (amenity, checkBox) ->
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                viewModel.onSocialAmenityChanged(amenity, isChecked)
            }
        }
    }

    private fun setupObserver() {
        // Internal Countable Amenities
        viewModel.icAmenityMap.observe(viewLifecycleOwner) { map ->
            logInfo("<------- Observing CountableInternalAmenity, $map ----->")
            map.forEach { (amenity, amenityDomain) ->
                val counterView = countableIntAmenitiesViewMap.getValue(amenity)
                if (counterView.count != amenityDomain.count) counterView.count = amenityDomain.count ?: 0
            }
        }

        // Internal Amenities
        viewModel.internalAmenityMap.observe(viewLifecycleOwner) { map ->
            logInfo("<------- Observing InternalAmenity, $map ----->")
            map.keys.forEach { amenity ->
                internalAmenitiesViewMap.getValue(amenity).isChecked = true
            }
        }

        // Social Amenities
        viewModel.socialAmenityMap.observe(viewLifecycleOwner) { map ->
            logInfo("<------- Observing SocialAmenity, $map ----->")
            map.keys.forEach { amenity ->
                socialAmenitiesViewMap.getValue(amenity).isChecked = true
            }
        }
    }

    // Helper Function
    private fun addAmenityWithCheckBox(amenityName: String): MaterialCheckBox {
        val textView = TextView(context).apply {
            text = amenityName
            this.textSize = 14f
            setLayoutParams(weightedWrapContentParams)
        }
        val checkBox = MaterialCheckBox(context).apply {
            setLayoutParams(wrapContentParams)
        }
        val llView = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(30, 0, 5, 0)
            setLayoutParams(matchParentWrapContentParams)

            // Add Label & Check Box
            addView(textView)
            addView(checkBox)

            setOnClickListener {
                checkBox.isChecked = !checkBox.isChecked
            }
        }

        binding.amenitiesContainer.addView(llView) // Adding to container
        return checkBox
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}