package com.example.houserentalapp.presentation.ui.listings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentMyPropertyBinding
import com.example.houserentalapp.presentation.ui.MainActivity
import com.example.houserentalapp.presentation.ui.property.CreatePropertyFragment
import com.example.houserentalapp.presentation.utils.extensions.logInfo

class MyPropertyFragment : Fragment(R.layout.fragment_my_property) {

    private lateinit var binding: FragmentMyPropertyBinding
    private lateinit var mainActivity: MainActivity

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMyPropertyBinding.bind(view)
        mainActivity = context as MainActivity

        setupUI()
        setupListeners()
    }

    fun setupUI() {
        // Always show bottom nav
        mainActivity.showBottomNav()

    }

    fun setupListeners() {
        binding.addPropertyBtn.setOnClickListener {
            logInfo("addProperty clicked requireActivity : ${requireActivity()}--------->")
            (requireActivity() as MainActivity).addFragment(
                CreatePropertyFragment(),
                true
            )
        }
    }
}