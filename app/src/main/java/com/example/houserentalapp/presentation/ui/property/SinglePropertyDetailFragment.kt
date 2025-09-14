package com.example.houserentalapp.presentation.ui.property

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.houserentalapp.R
import com.example.houserentalapp.data.repo.PropertyRepoImpl
import com.example.houserentalapp.data.repo.UserPropertyRepoImpl
import com.example.houserentalapp.databinding.FragmentSinglePropertyDetailBinding
import com.example.houserentalapp.domain.model.AmenityDomain
import com.example.houserentalapp.domain.model.Property
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.model.enums.AmenityType
import com.example.houserentalapp.domain.model.enums.TenantType
import com.example.houserentalapp.domain.usecase.PropertyUseCase
import com.example.houserentalapp.domain.usecase.TenantRelatedPropertyUseCase
import com.example.houserentalapp.presentation.model.PropertyWithActionsUI
import com.example.houserentalapp.presentation.utils.helpers.fromEpoch
import com.example.houserentalapp.presentation.ui.MainActivity
import com.example.houserentalapp.presentation.ui.property.adapter.PropertyImagesViewAdapter
import com.example.houserentalapp.presentation.ui.property.viewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.ui.property.viewmodel.SinglePropertyDetailViewModel
import com.example.houserentalapp.presentation.ui.property.viewmodel.SinglePropertyDetailViewModelFactory
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.dpToPx
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.showToast
import com.example.houserentalapp.presentation.utils.helpers.getAmenityDrawable
import com.example.houserentalapp.presentation.utils.helpers.setSystemBarBottomPadding

/* TODO
    1. FIX: Maintenance alignment
 */
class SinglePropertyDetailFragment : Fragment(R.layout.fragment_single_property_detail) {
    private lateinit var binding: FragmentSinglePropertyDetailBinding
    private lateinit var adapter: PropertyImagesViewAdapter
    private lateinit var mainActivity: MainActivity
    private lateinit var currentUser: User
    private lateinit var viewModel: SinglePropertyDetailViewModel
    private val sharedDataViewModel: SharedDataViewModel by activityViewModels()

    private var propertyId: Long = -1L
    private var isTenantView = false
    private var hideAndShowBottomNav = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        propertyId = arguments?.getLong(PROPERTY_ID_KEY) ?: run {
            logError("Selected property id is not found in bundle")
            parentFragmentManager.popBackStack()
            return
        }
        isTenantView = arguments?.getBoolean(IS_TENANT_VIEW_KEY) ?: false
        hideAndShowBottomNav = arguments?.getBoolean(HIDE_AND_SHOW_BOTTOM_NAV_KEY) ?: false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSinglePropertyDetailBinding.bind(view)
        // Take Current User
        currentUser = sharedDataViewModel.currentUser ?: run {
            mainActivity.showToast("Login again...")
            mainActivity.finish()
            return
        }

        logDebug("Received arguments \n" +
                "PROPERTY_ID_KEY: $propertyId" +
                "IS_TENANT_VIEW_KEY: $isTenantView" +
                "HIDE_AND_SHOW_BOTTOM_NAV_KEY: $hideAndShowBottomNav"
        )

        setupUI()
        setupViewModel()
        setListeners()
        setObservers()

        // Initial Fetch
        if (viewModel.propertyResult.value !is ResultUI.Success)
            viewModel.loadProperty(propertyId, isTenantView)
    }

    fun setupUI() {
        // Add paddingBottom to avoid system bar overlay
        setSystemBarBottomPadding(binding.root)

        // Always hide bottom nav
        mainActivity.hideBottomNav()

        with(binding) {
            // Image ViewPager 2
            adapter = PropertyImagesViewAdapter()
            viewPager2.apply {
                this.adapter = this@SinglePropertyDetailFragment.adapter
                beginFakeDrag()
                fakeDragBy(-2.0f)
                endFakeDrag()
            }
        }
    }

    fun setupViewModel() {
        val context = requireActivity()
        val getPropertyUseCase = PropertyUseCase(PropertyRepoImpl(context))
        val propertyUserActionUseCase = TenantRelatedPropertyUseCase(UserPropertyRepoImpl(context))
        val factory = SinglePropertyDetailViewModelFactory(
            getPropertyUseCase,
            propertyUserActionUseCase,
            currentUser
        )
        viewModel = ViewModelProvider(this, factory).get(SinglePropertyDetailViewModel::class.java)
    }

    fun setListeners() {
        with(binding) {
          // ToolBar Listeners
            toolbar.setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }

            toolbar.setOnMenuItemClickListener { item ->
                if(item.itemId == R.id.tbar_shortlist)
                    viewModel.toggleFavourite(propertyId)
                true
            }
        }
    }

    fun updateShortlistIcon(isShortlisted: Boolean) {
        val icon = if (isShortlisted)
            R.drawable.baseline_favorite_filled_primary_color
        else
            R.drawable.outline_favorite

        binding.toolbar.menu.findItem(R.id.tbar_shortlist).setIcon(icon)
    }

    @SuppressLint("UseCompatTextViewDrawableApis") // Im suppressing the warning because my min sdk is > 23
    private fun getAmenityView(amenity: AmenityDomain): TextView {
        val fourDpInPx = 4.dpToPx(requireActivity())
        val drawable = getAmenityDrawable(amenity)

        return TextView(context).apply {
            text = if (amenity.type == AmenityType.INTERNAL_COUNTABLE)
                "${amenity.name.readable} : ${amenity.count}"
            else
                amenity.name.readable
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

    private fun bindBasicCardDetails(property: Property) {
        with(binding) {
            tvPropertyName.text =
                getString(R.string.pre_for_suff, property.name, property.lookingTo.readable)
            tvAddress.text = property.address.let { "${it.street}, ${it.locality}, ${it.city}" }
            tvFurnishingType.text = property.furnishingType.readable
            tvBuiltArea.text = getString(R.string.area_with_sq_ft, property.builtUpArea)
            tvPreferredTenant.text = property.preferredTenantType.joinToString(",") { it.readable }
            tvRent.text = getString(R.string.property_price, property.price)
            tvMaintenance.text = if (property.isMaintenanceSeparate)
                getString(R.string.property_price, property.maintenanceCharges)
            else
                "included"
            tvSecurityDeposit.text = getString(R.string.property_price, property.securityDepositAmount)
        }
    }

    private fun bindOverviewCardDetails(property: Property) {
        with(binding) {
            tvPropertyType.text = property.type.readable
            tvBHK.text = property.bhk.readable
            tvIsPetAllowed.text = if (property.isPetAllowed) "Yes allowed" else "Not allowed"
            tvBathroomCount.text = property.bathRoomCount.toString()
            tvPostedOn.text = property.createdAt.fromEpoch()
            tvAvailableFrom.text = property.availableFrom.fromEpoch()
            tvOpenParking.text = property.countOfOpenParking.toString()
            tvCoveredParking.text = property.countOfCoveredParking.toString()

            var tenants = property.preferredTenantType.joinToString(", ") { it.readable }
            tenants += if (TenantType.BACHELORS in property.preferredTenantType &&
                property.preferredBachelorType != null)
                "(${property.preferredBachelorType.readable.lowercase()})"
            else ""
            tvPreferredTenant.text = tenants

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

    private fun bindAmenitiesCardDetails(amenities: List<AmenityDomain>) {
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

            val internalAmenities = mutableListOf<AmenityDomain>()
            val internalCountableAmenities = mutableListOf<AmenityDomain>()
            val socialAmenities = mutableListOf<AmenityDomain>()

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

    private fun bindPropertyDetails(propertyUI: PropertyWithActionsUI) {
        // Load Images
        adapter.setPropertyImages(propertyUI.property.images)

        // Load Details
        binding.collapsingTBarLayout.title = propertyUI.property.name
        bindBasicCardDetails(propertyUI.property)
        bindOverviewCardDetails(propertyUI.property)
        bindAmenitiesCardDetails(propertyUI.property.amenities)
    }

    fun setObservers() {
        with(binding) {
            viewModel.propertyResult.observe(viewLifecycleOwner) { result ->
                when(result) {
                    is ResultUI.Success<PropertyWithActionsUI> -> {
                        bindPropertyDetails(result.data)
                    }
                    is ResultUI.Error -> {
                        logInfo("error.... ${result.message}")
                    }
                    ResultUI.Loading -> {
                        logInfo("loading....")
                    }
                }
            }

            viewModel.isShortlisted.observe(viewLifecycleOwner) {
                updateShortlistIcon(it)
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        if (hideAndShowBottomNav)
            mainActivity.showBottomNav()
    }

    companion object {
        const val PROPERTY_ID_KEY = "propertyId"
        const val IS_TENANT_VIEW_KEY = "isTenantView"
        const val HIDE_AND_SHOW_BOTTOM_NAV_KEY = "hideAndShow"
    }
}