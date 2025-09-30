package com.example.houserentalapp.presentation.ui.property

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
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
import com.example.houserentalapp.domain.model.PropertyImage
import com.example.houserentalapp.domain.model.User
import com.example.houserentalapp.domain.model.enums.AmenityType
import com.example.houserentalapp.domain.model.enums.TenantType
import com.example.houserentalapp.domain.usecase.PropertyUseCase
import com.example.houserentalapp.domain.usecase.UserPropertyUseCase
import com.example.houserentalapp.presentation.model.PropertyUI
import com.example.houserentalapp.presentation.ui.FragmentArgKey
import com.example.houserentalapp.presentation.utils.helpers.fromEpoch
import com.example.houserentalapp.presentation.ui.NavigationDestination
import com.example.houserentalapp.presentation.ui.base.BaseFragment
import com.example.houserentalapp.presentation.ui.interfaces.BottomNavController
import com.example.houserentalapp.presentation.ui.property.adapter.PropertyImagesViewAdapter
import com.example.houserentalapp.presentation.ui.sharedviewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.ui.property.viewmodel.SinglePropertyDetailViewModel
import com.example.houserentalapp.presentation.ui.property.viewmodel.SinglePropertyDetailViewModelFactory
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.DrawablePosition
import com.example.houserentalapp.presentation.utils.extensions.dpToPx
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.logWarning
import com.example.houserentalapp.presentation.utils.extensions.setDrawable
import com.example.houserentalapp.presentation.utils.helpers.getAmenityDrawable
import com.example.houserentalapp.presentation.utils.helpers.setSystemBarBottomPadding

class SinglePropertyDetailFragment : BaseFragment(R.layout.fragment_single_property_detail) {
    private lateinit var binding: FragmentSinglePropertyDetailBinding
    private lateinit var adapter: PropertyImagesViewAdapter
    private lateinit var bottomNavController: BottomNavController
    private lateinit var currentUser: User
    private lateinit var viewModel: SinglePropertyDetailViewModel
    private val sharedDataViewModel: SharedDataViewModel by activityViewModels()
    private val _context: Context get() = requireContext()

    private var propertyId: Long = -1L
    private var isTenantView = false
    private var hideAndShowBottomNav = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        bottomNavController = context as BottomNavController
    }

    // onCreate() for reading arguments.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        propertyId = arguments?.getLong(FragmentArgKey.PROPERTY_ID) ?: run {
            logError("Selected property id is not found in bundle")
            parentFragmentManager.popBackStack()
            return
        }
        isTenantView = arguments?.getBoolean(FragmentArgKey.IS_TENANT_VIEW) ?: false
        hideAndShowBottomNav = arguments?.getBoolean(FragmentArgKey.HIDE_AND_SHOW_BOTTOM_NAV) ?: false

        logDebug("Received arguments \n" +
                "PROPERTY_ID_KEY: $propertyId" +
                "IS_TENANT_VIEW_KEY: $isTenantView" +
                "HIDE_AND_SHOW_BOTTOM_NAV_KEY: $hideAndShowBottomNav"
        )

        // Take Current User
        currentUser = sharedDataViewModel.currentUserData
    }

    // onViewCreated() for applying arguments to UI
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSinglePropertyDetailBinding.bind(view)

        setupUI()
        setupViewModel()
        setListeners()
        setObservers()

        // Initial Fetch
        if (savedInstanceState == null) {
            if (isTenantView)
                viewModel.loadPropertyWithActions(propertyId)
            else
                viewModel.loadProperty(propertyId)
        }
    }

    fun setupUI() {
        // Add paddingBottom to avoid system bar overlay
        setSystemBarBottomPadding(binding.root)

        // Always hide bottom nav
        bottomNavController.hideBottomNav()

        with(binding) {
            // Image ViewPager 2
            adapter = PropertyImagesViewAdapter(::onImageClick)
            viewPager2.apply {
                this.adapter = this@SinglePropertyDetailFragment.adapter
                beginFakeDrag()
                fakeDragBy(-2.0f)
                endFakeDrag()
            }

            // Render Toolbar icons
            if (isTenantView) {
                toolbar.menu.findItem(R.id.tbar_shortlist).apply {
                    isVisible = true
                    isEnabled = true
                }
                contactDetailsCard.visibility = View.VISIBLE

                val drawablePadding = 4.dpToPx()
                tvLabelName.apply {
                    setDrawable(
                        R.drawable.baseline_person_24,
                        16,
                        16,
                        DrawablePosition.LEFT
                    )
                    compoundDrawablePadding = drawablePadding
                }
                tvLabelPhone.apply {
                    setDrawable(
                        R.drawable.baseline_call_24,
                        16,
                        16,
                        DrawablePosition.LEFT
                    )
                    compoundDrawablePadding = drawablePadding
                }
                tvLabelEmail.apply {
                    setDrawable(
                        R.drawable.baseline_email_24,
                        16,
                        16,
                        DrawablePosition.LEFT
                    )
                    compoundDrawablePadding = drawablePadding
                }
            } else {
                toolbar.menu.findItem(R.id.tbar_edit).apply {
                    isVisible = true
                    isEnabled = true
                }

                contactDetailsCard.visibility = View.GONE
            }
        }
    }

    fun setupViewModel() {
        val context = requireActivity()
        val propertyUC = PropertyUseCase(PropertyRepoImpl(context))
        val userPropertyUC = UserPropertyUseCase(UserPropertyRepoImpl(context))
        val factory = SinglePropertyDetailViewModelFactory(
            propertyUC,
            userPropertyUC,
            currentUser
        )
        viewModel = ViewModelProvider(this, factory).get(SinglePropertyDetailViewModel::class.java)
    }

    fun onEditIconClick() {
        val bundle = Bundle().apply {
            putLong(FragmentArgKey.PROPERTY_ID, propertyId)
            putBoolean(FragmentArgKey.HIDE_AND_SHOW_BOTTOM_NAV, false)
        }
        navigateTo(NavigationDestination.CreateProperty(bundle))
    }

    fun setListeners() {
        with(binding) {
          // ToolBar Listeners
            toolbar.setNavigationOnClickListener {
                navigationHandler.navigateBack()
            }

            toolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.tbar_shortlist -> handleShortlistToggle(propertyId)
                    R.id.tbar_edit -> {
                        if (isTenantView)
                            logWarning("A Tenant should not edit an property")
                        else
                            onEditIconClick()
                    }
                }

                true
            }

            btnViewContactDetails.setOnClickListener {
                viewModel.storeUserInterest(propertyId)
            }
        }
    }
    private fun handleShortlistToggle(propertyId: Long) {
        viewModel.toggleShortlist(
            propertyId,
            { isShortlisted ->
                val message = if (isShortlisted)
                    "Added to shortlisted"
                else
                    "Removed from shortlisted"
                Toast.makeText(_context, message, Toast.LENGTH_SHORT).show()

                // Trigger Update Event In SharedViewModel
                sharedDataViewModel.setUpdatedPropertyId(propertyId)
            },
            {
                Toast.makeText(_context, "Retry later", Toast.LENGTH_SHORT).show()
            }
        )
    }

    fun onImageClick(propertyImages: List<PropertyImage>) {
        sharedDataViewModel.imageSources = propertyImages.map { it.imageSource }
        navigateTo(NavigationDestination.MultipleImages())
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
        val fourDpInPx = 4.dpToPx()
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
            tvMaintenance.text = getString(
                R.string.property_price,
                if (property.isMaintenanceSeparate) property.maintenanceCharges else 0
            )
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

    private fun bindContactCardDetails(landlordUser: User?) {
        with(binding) {
            if (landlordUser != null) {
                flUnlockContactDetails.visibility = View.GONE
                tvValueName.text = landlordUser.name
                tvValuePhone.text = landlordUser.phone
                tvValueEmail.text = landlordUser.email ?: getString(R.string.not_available)
            }
            else
                flUnlockContactDetails.visibility = View.VISIBLE
        }
    }

    private fun bindPropertyDetails(property: Property) {
        // Load Images
        if (property.images.isNotEmpty()) {
            binding.imgProperty.visibility = View.GONE
            adapter.setPropertyImages(property.images)
        }
        else
            binding.imgProperty.apply {
                visibility = View.VISIBLE
                setImageResource(R.drawable.no_image)
            }

        // Load Details
        binding.collapsingTBarLayout.title = property.name
        bindBasicCardDetails(property)
        bindOverviewCardDetails(property)
        bindAmenitiesCardDetails(property.amenities)
    }

    fun setObservers() {
        with(binding) {
            if (isTenantView)
                viewModel.propertyUIResult.observe(viewLifecycleOwner) { result ->
                    when(result) {
                        is ResultUI.Success<PropertyUI> -> {
                            if (result.data.propertyInfoChanged)
                                bindPropertyDetails(result.data.property)
                            if (result.data.shortlistStateChanged)
                                updateShortlistIcon(result.data.isShortlisted)
                            if (result.data.interestedStateChanged && result.data.isInterested)
                                bindContactCardDetails(result.data.landlordUser)

                            viewModel.clearChangeFlags()
                        }
                        is ResultUI.Error -> {
                            logInfo("error.... ${result.message}")
                        }
                        ResultUI.Loading -> {
                            logInfo("loading....")
                        }
                    }
                }
            else
                viewModel.onlyPropertyDetailsRes.observe(viewLifecycleOwner) { result ->
                    when(result) {
                        is ResultUI.Success<Property> -> {
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
        }
    }

    override fun onDetach() {
        super.onDetach()
        if (hideAndShowBottomNav)
            bottomNavController.showBottomNav()
    }

    companion object {
        const val PROPERTY_ID_KEY = "propertyId"
        const val IS_TENANT_VIEW_KEY = "isTenantView"
        const val HIDE_AND_SHOW_BOTTOM_NAV_KEY = "hideAndShow"
    }
}