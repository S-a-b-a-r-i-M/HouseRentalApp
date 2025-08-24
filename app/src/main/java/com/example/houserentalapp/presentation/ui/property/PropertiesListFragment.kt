package com.example.houserentalapp.presentation.ui.property

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentPropertiesListBinding
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.domain.utils.createPropertiesListViewModel
import com.example.houserentalapp.presentation.ui.MainActivity
import com.example.houserentalapp.presentation.ui.property.viewmodel.PropertiesListViewModel
import com.example.houserentalapp.presentation.utils.ResultUI
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.logWarning

class PropertiesListFragment : Fragment(R.layout.fragment_properties_list) {
    private lateinit var binding: FragmentPropertiesListBinding
    private lateinit var mainActivity: MainActivity
    private lateinit var propertiesAdapter: PropertiesAdapter
    private val propertiesListViewModel: PropertiesListViewModel by activityViewModels {
        createPropertiesListViewModel()
    }
    private var isScrolling: Boolean = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = requireActivity() as MainActivity
        mainActivity.hideBottomNav()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPropertiesListBinding.bind(view)

        setupUI()
        setupObservers()

        if (savedInstanceState == null)
            propertiesListViewModel.loadPropertySummaries()
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
                    propertiesListViewModel.loadPropertySummaries()
                }
            }
        }
    }

    fun setupUI() {
        with(binding) {
            // RecyclerView
            propertiesAdapter = PropertiesAdapter()
            rvProperty.apply {
                layoutManager = LinearLayoutManager(requireActivity())
                adapter = propertiesAdapter
                addOnScrollListener(scrollListener)
            }
        }
    }

    private fun setupObservers() {
        propertiesListViewModel.propertySummariesState.observe(viewLifecycleOwner) { result ->
            when(result) {
                is ResultUI.Success<List<PropertySummary>> -> {
                    logInfo("success")
                    propertiesAdapter.setDataList(result.data)
                }
                is ResultUI.Error -> {
                    logError("error occured")
                }
                ResultUI.Loading -> {
                    logInfo("loading")
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        mainActivity.showBottomNav()
    }
}