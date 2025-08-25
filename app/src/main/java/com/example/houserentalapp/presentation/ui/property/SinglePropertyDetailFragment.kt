package com.example.houserentalapp.presentation.ui.property

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.houserentalapp.R
import com.example.houserentalapp.data.repo.PropertyRepoImpl
import com.example.houserentalapp.databinding.FragmentSinglePropertyDetailBinding
import com.example.houserentalapp.domain.model.Property
import com.example.houserentalapp.domain.usecase.GetPropertyUseCase
import com.example.houserentalapp.presentation.ui.MainActivity
import com.example.houserentalapp.presentation.ui.property.viewmodel.SinglePropertyDetailViewModel
import com.example.houserentalapp.presentation.ui.property.viewmodel.SinglePropertyDetailViewModelFactory
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.helpers.setSystemBarBottomPadding

class SinglePropertyDetailFragment : Fragment() {
    private lateinit var binding: FragmentSinglePropertyDetailBinding
    private lateinit var viewModel: SinglePropertyDetailViewModel
    private lateinit var adapter: PropertyImageViewAdapter

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

        val propertyId = arguments?.getLong("property_id") ?: run {
            logError("Selected property id is not found in bundle")
            parentFragmentManager.popBackStack()
        }

        logInfo("Selected property id : $propertyId")

        // Add paddingBottom to avoid system bar overlay
        setSystemBarBottomPadding(binding.root)

        setupUI()
        setupViewModel()

        setListeners()
        setObservers()

        if (savedInstanceState == null)
            viewModel.loadProperty(propertyId as Long)
    }

    fun setupUI() {
        with(binding) {
            adapter = PropertyImageViewAdapter()
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
            backImgBtn.root.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }

    fun setObservers() {
        with(binding) {
            viewModel.propertyResult.observe(viewLifecycleOwner) { result ->
                when(result) {
                    is ResultUI.Error -> {

                    }
                    ResultUI.Loading -> {

                    }
                    is ResultUI.Success<Property> ->
                        adapter.setPropertyImages(result.data.images)
                }

            }
        }
    }
}