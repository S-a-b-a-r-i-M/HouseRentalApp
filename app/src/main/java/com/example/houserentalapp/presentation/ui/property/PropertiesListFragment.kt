package com.example.houserentalapp.presentation.ui.property

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R
import com.example.houserentalapp.data.repo.PropertyRepoImpl
import com.example.houserentalapp.data.repo.UserPropertyRepoImpl
import com.example.houserentalapp.databinding.FragmentPropertiesListBinding
import com.example.houserentalapp.domain.usecase.GetPropertyUseCase
import com.example.houserentalapp.domain.usecase.TenantRelatedPropertyUseCase
import com.example.houserentalapp.presentation.model.PropertySummaryUI
import com.example.houserentalapp.presentation.ui.MainActivity
import com.example.houserentalapp.presentation.ui.property.adapter.PropertiesAdapter
import com.example.houserentalapp.presentation.ui.property.viewmodel.PropertiesListViewModel
import com.example.houserentalapp.presentation.ui.property.viewmodel.PropertiesListViewModelFactory
import com.example.houserentalapp.presentation.ui.property.viewmodel.SharedDataViewModel
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.logWarning
import com.example.houserentalapp.presentation.utils.helpers.setSystemBarBottomPadding

class PropertiesListFragment : Fragment(R.layout.fragment_properties_list) {
    private lateinit var binding: FragmentPropertiesListBinding
    private lateinit var mainActivity: MainActivity
    private lateinit var propertiesAdapter: PropertiesAdapter
    private lateinit var propertiesListViewModel: PropertiesListViewModel
    private val sharedDataViewModel: SharedDataViewModel by activityViewModels()

    private var isScrolling: Boolean = false
    private var hideBottomNav = false
    private var onlyShortlisted = false
    private var hideToolBar = false
    private var hideFabButton = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPropertiesListBinding.bind(view)
        // mainActivity = requireActivity() as MainActivity // Crashing on quick rotations
        mainActivity = context as MainActivity

        hideBottomNav = sharedDataViewModel.fPropertiesListMap[HIDE_BOTTOM_NAV_KEY] as? Boolean ?: false
        hideToolBar = sharedDataViewModel.fPropertiesListMap[HIDE_TOOLBAR_KEY] as? Boolean ?: false
        hideFabButton = sharedDataViewModel.fPropertiesListMap[HIDE_FAB_BUTTON_KEY] as? Boolean ?: false
        onlyShortlisted = sharedDataViewModel.fPropertiesListMap[ONLY_SHORTLISTED_KEY] as? Boolean ?: false

        setupUI()
        setupViewModel()
        setupListeners()
        setupObservers()

        if (propertiesListViewModel.propertySummariesResult.value !is ResultUI.Success)
            propertiesListViewModel.loadPropertySummaries(onlyShortlisted)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(HIDE_BOTTOM_NAV_KEY, hideBottomNav)
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            val layoutManger = recyclerView.layoutManager as LinearLayoutManager

            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                isScrolling = true
                return // No need to fetch new items while scrolling
            }
            else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                isScrolling = false
                if (!propertiesListViewModel.hasMore()) return

                val lastVisibleItemPosition =
                    layoutManger.findLastCompletelyVisibleItemPosition() // index
                val totalItemCount = recyclerView.adapter?.itemCount ?: run {
                    logWarning("totalItemCount is not accessible")
                    return
                }
                val shouldLoadMore = (lastVisibleItemPosition + 1) >= totalItemCount
                if (shouldLoadMore) {
                    logInfo("<----------- from onScroll State changed ---------->")
                    propertiesListViewModel.loadPropertySummaries(onlyShortlisted)
                }
            }
        }
    }

    fun setupUI() {
        if (hideBottomNav)
            mainActivity.hideBottomNav()
        else
            mainActivity.showBottomNav()

        // Add paddingBottom to avoid system bar overlay
        setSystemBarBottomPadding(binding.root)

        with(binding) {
          // RecyclerView
            propertiesAdapter = PropertiesAdapter(
                ::handleOnPropertyClick, ::handleShortlistToggle
            )
            rvProperty.apply {
                layoutManager = LinearLayoutManager(requireActivity())
                adapter = propertiesAdapter
                addOnScrollListener(scrollListener)
            }

            if (hideToolBar)
                toolBarLayout.visibility = View.GONE

            if (hideFabButton)
                fabShortlists.visibility = View.GONE
        }
    }

    fun setupViewModel() {
        val getPropertyUC = GetPropertyUseCase(PropertyRepoImpl(mainActivity))
        val propertyUserActionUC = TenantRelatedPropertyUseCase(UserPropertyRepoImpl(mainActivity))
        val currentUser = mainActivity.getCurrentUser()
        val factory = PropertiesListViewModelFactory(getPropertyUC, propertyUserActionUC, currentUser)
        propertiesListViewModel = ViewModelProvider(this, factory)
            .get(PropertiesListViewModel::class.java)
    }

    fun setupListeners() {
        with(binding) {
            backImgBtn.root.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun handleOnPropertyClick(propertyId: Long) {
        val destinationFragment = SinglePropertyDetailFragment()
        destinationFragment.arguments = Bundle().apply {
            putLong(SinglePropertyDetailFragment.PROPERTY_ID_KEY, propertyId)
            putBoolean(SinglePropertyDetailFragment.IS_TENANT_VIEW_KEY, true)
        }

        mainActivity.loadFragment(destinationFragment, true)
    }

    private fun handleShortlistToggle(propertyId: Long) {
        propertiesListViewModel.togglePropertyShortlist(
            propertyId,
            { isShortlisted ->
                val message = if (isShortlisted)
                    "Added to shortlisted"
                else
                    "Removed from shortlisted"
                Toast.makeText(mainActivity, message, Toast.LENGTH_SHORT).show()
            },
            {
                Toast.makeText(mainActivity, "Retry later", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun setupObservers() {
        propertiesListViewModel.propertySummariesResult.observe(viewLifecycleOwner) { result ->
            when(result) {
                is ResultUI.Success<List<PropertySummaryUI>> -> {
                    logInfo("success")
                    propertiesAdapter.setDataList(result.data)
                    hideProgressBar()
                }
                is ResultUI.Error -> {
                    hideProgressBar()
                    logError("error occured")
                }
                ResultUI.Loading -> {
                    showProgressBar()
                    logInfo("loading")
                }
            }
        }
    }

    fun showProgressBar() {
        isScrolling
        binding.progressBar.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    override fun onDetach() {
        super.onDetach()
        if (hideBottomNav) // Only Show if we hide it
            mainActivity.showBottomNav()
    }

    companion object {
        const val HIDE_BOTTOM_NAV_KEY = "hideBottomNav"
        const val ONLY_SHORTLISTED_KEY = "onlyShortlisted"
        const val HIDE_TOOLBAR_KEY = "hideToolBar"
        const val HIDE_FAB_BUTTON_KEY= "hideFabButton"
    }
}