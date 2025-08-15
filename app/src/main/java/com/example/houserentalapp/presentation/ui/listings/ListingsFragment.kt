package com.example.houserentalapp.presentation.ui.listings

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentListingsBinding
import com.example.houserentalapp.presentation.utils.extensions.loadFragment
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.extensions.logInfo

class ListingsFragment : Fragment() {
    private lateinit var binding: FragmentListingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_listings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentListingsBinding.bind(view)

        Log.i(TAG, "requireActivity : ${requireActivity()}" )

        setOnClickListeners()

        // ON FRAGMENT FIRST CREATION
        logDebug("savedInstanceState ------> $savedInstanceState")
        if (savedInstanceState == null)
            binding.myPropertiesBtn.performClick() // PERFORM CLICK
    }

    private fun setOnClickListeners() {
        val appCompatActivity = requireActivity() as AppCompatActivity
        with(binding) {
            var isMyPropertiesBtnReselected = false
            myPropertiesBtn.setOnClickListener {
                println("myPropertiesBtn setOnClickListener")
                if (isMyPropertiesBtnReselected) {
                    isMyPropertiesBtnReselected = false
                    return@setOnClickListener
                }

                appCompatActivity.loadFragment(
                    listingsFragmentContainer.id,
                    MyPropertyFragment()
                )
            }

            myPropertiesBtn.addOnCheckedChangeListener { btn, isChecked ->
                println("myPropertiesBtn addOnCheckedChangeListener")
                if (!isChecked && !leadsBtn.isChecked) {
                    btn.isChecked = true // Changing the checked state of the btn won't trigger the onclick
                    isMyPropertiesBtnReselected = true
                }
            }

            var isLeadsBtnReselected = false
            leadsBtn.setOnClickListener {
                if (isLeadsBtnReselected) {
                    isLeadsBtnReselected = false
                    return@setOnClickListener
                }

                appCompatActivity.loadFragment(
                    listingsFragmentContainer.id,
                    LeadsFragment()
                )
            }

            leadsBtn.addOnCheckedChangeListener { btn, isChecked ->
                if (!isChecked && !myPropertiesBtn.isChecked) {
                    btn.isChecked = true
                    isLeadsBtnReselected = true
                }
            }
        }
    }

    companion object {
        private const val TAG = "ListingsFragment"
    }
}