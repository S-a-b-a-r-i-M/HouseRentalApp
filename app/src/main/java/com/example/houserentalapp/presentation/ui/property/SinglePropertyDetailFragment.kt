package com.example.houserentalapp.presentation.ui.property

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.lifecycle.ViewModelProvider
import com.example.houserentalapp.R
import com.example.houserentalapp.data.repo.PropertyRepoImpl
import com.example.houserentalapp.databinding.FragmentSinglePropertyDetailBinding
import com.example.houserentalapp.domain.model.Amenity
import com.example.houserentalapp.domain.model.Property
import com.example.houserentalapp.domain.model.enums.AmenityType
import com.example.houserentalapp.domain.usecase.GetPropertyUseCase
import com.example.houserentalapp.presentation.utils.helpers.fromEpoch
import com.example.houserentalapp.presentation.ui.MainActivity
import com.example.houserentalapp.presentation.ui.property.adapter.PropertyImagesViewAdapter
import com.example.houserentalapp.presentation.ui.property.viewmodel.SinglePropertyDetailViewModel
import com.example.houserentalapp.presentation.ui.property.viewmodel.SinglePropertyDetailViewModelFactory
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.dpToPx
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.helpers.getAmenityDrawable
import com.example.houserentalapp.presentation.utils.helpers.setSystemBarBottomPadding

class SinglePropertyDetailFragment : Fragment() {
    private lateinit var binding: FragmentSinglePropertyDetailBinding
    private lateinit var viewModel: SinglePropertyDetailViewModel
    private lateinit var adapter: PropertyImagesViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_single_property_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSinglePropertyDetailBinding.bind(view)

        val propertyId = arguments?.getLong("propertyId") ?: run {
            logError("Selected property id is not found in bundle")
            parentFragmentManager.popBackStack()
        }

        logInfo("Selected property id : $propertyId")

        setupUI()
        setupViewModel()

        setListeners()
        setObservers()

        if (savedInstanceState == null)
            viewModel.loadProperty(propertyId as Long)
    }

    fun setupUI() {
        // Add paddingBottom to avoid system bar overlay
        setSystemBarBottomPadding(binding.root)
        with(binding) {
            adapter = PropertyImagesViewAdapter()
            viewPager2.adapter = adapter
        }
    }

    fun setupViewModel() {
        val mainActivity = requireActivity() as MainActivity
        val useCase = GetPropertyUseCase(PropertyRepoImpl(mainActivity))
        val factory = SinglePropertyDetailViewModelFactory(useCase)
        viewModel = ViewModelProvider(mainActivity, factory).get(SinglePropertyDetailViewModel::class.java)
    }

    fun setListeners() {
        with(binding) {
            toolbar.setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }

            toolbar.setOnMenuItemClickListener { item ->
                if(item.itemId == R.id.tbar_shortlist)
                    logInfo("tbar_shortlist clicked")

                true
            }
        }
    }

    @SuppressLint("UseCompatTextViewDrawableApis") // Im suppressing the warning because my min sdk is > 23
    private fun getAmenityView(amenity: Amenity): TextView {
        val fourDpInPx = 4.dpToPx(requireActivity())
        val drawable = getAmenityDrawable(amenity)

        return TextView(context).apply {
            text = if (amenity.type == AmenityType.INTERNAL_COUNTABLE)
                "${amenity.name} : ${amenity.count}"
            else
                amenity.name
            gravity = Gravity.CENTER
            setTextAppearance(R.style.TextAppearance_App_LabelMedium)
            setPadding(fourDpInPx)

            // Add Drawable At the top
            setCompoundDrawablesWithIntrinsicBounds(0, drawable, 0, 0)
            setCompoundDrawableTintList(
                ColorStateList.valueOf(context.resources.getColor(R.color.gray_dark))
            )

            // Add Layout Params
            setLayoutParams(
                GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(fourDpInPx)
                }
            )
        }
    }

    private fun bindPropertyMinDetails(property: Property) {
        with(binding) {
            collapsingTBarLayout.title = property.name
            tvPropertyName.text = "${property.name} for ${property.lookingTo.readable}"
            tvAddress.text = property.address.let { "${it.streetName}, ${it.locality}, ${it.city}" }
            tvFurnishingType.text = property.furnishingType.readable
            tvBuiltArea.text = "${property.builtUpArea} sq.ft."
            tvPreferredTenant.text = property.preferredTenantType.joinToString(",") { it.readable }
            tvRent.text = "₹ ${property.price}"
            tvMaintenance.text = if (property.isMaintenanceSeparate)
                "₹ ${property.maintenanceCharges}"
            else
                "included"
            tvSecurityDeposit.text = "₹ ${property.securityDepositAmount}"
        }
    }

    private fun bindPropertyOverviewDetails(property: Property) {
        with(binding) {
            tvPropertyType.text = property.type.readable
            tvBHK.text = property.bhk.readable
            tvIsPetAllowed.text = if (property.isPetAllowed) "Yes allowed" else "Not allowed"
            tvBathroomCount.text = property.bathRoomCount.toString()
            tvPostedOn.text = property.createdAt.fromEpoch()
            tvAvailableFrom.text = property.availableFrom.fromEpoch()
            tvOpenParking.text = property.countOfOpenParking.toString()
            tvCoveredParking.text = property.countOfCoveredParking.toString()
            if (property.description != null && property.description.isNotBlank())
                tvDescription.text = property.description
            else
                tvDescription.apply {
                    text = context.getString(R.string.no_description)
                    gravity = Gravity.CENTER
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    setTextAppearance(R.style.TextAppearance_App_Body2)
                    setTextColor(context.resources.getColor(R.color.gray_medium))
                }
        }
    }

    private fun bindPropertyAmenitiesDetails(amenities: List<Amenity>) {
        with(binding) {
            if (amenities.isEmpty()) {
                val textView = TextView(context).apply {
                    text = context.getString(R.string.no_amenities_available)
                    gravity = Gravity.CENTER
                    setTextAppearance(R.style.TextAppearance_App_Body2)
                    setTextColor(context.resources.getColor(R.color.gray_medium))
                }

                llAmenities.addView(textView)
                return
            }

            val internalAmenities = mutableListOf<Amenity>()
            val internalCountableAmenities = mutableListOf<Amenity>()
            val socialAmenities = mutableListOf<Amenity>()

            amenities.forEach {
                when (it.type) {
                    AmenityType.INTERNAL -> internalAmenities.add(it)
                    AmenityType.INTERNAL_COUNTABLE -> internalCountableAmenities.add(it)
                    AmenityType.SOCIAL -> socialAmenities.add(it)
                }
            }

            internalCountableAmenities.forEach {
                gridsOfInternalAmenities.addView(getAmenityView(it))
            }
            internalAmenities.forEach {
                gridsOfInternalAmenities.addView(getAmenityView(it))
            }
            socialAmenities.forEach {
                gridsOfSocialAmenities.addView(getAmenityView(it))
            }

            // Separator
            amenitiesSeparator.visibility = if (
                (internalAmenities.isNotEmpty() || internalCountableAmenities.isNotEmpty()) &&
                socialAmenities.isNotEmpty()
            )
                View.VISIBLE
            else
                View.GONE
        }
    }

    private fun bindPropertyDetails(property: Property) {
        // Load Images
        adapter.setPropertyImages(property.images)

        // Load Details
        bindPropertyMinDetails(property)
        bindPropertyOverviewDetails(property)
        bindPropertyAmenitiesDetails(property.amenities)
    }

    fun setObservers() {
        with(binding) {
            viewModel.propertyResult.observe(viewLifecycleOwner) { result ->
                when(result) {
                    is ResultUI.Error -> {
                        logInfo("error.... ${result.message}")
                    }
                    ResultUI.Loading -> {
                        logInfo("loading....")
                    }
                    is ResultUI.Success<Property> -> {
                        bindPropertyDetails(result.data)
                    }
                }
            }
        }
    }
}