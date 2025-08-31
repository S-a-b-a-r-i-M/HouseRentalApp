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

class MyPropertyFragment : Fragment() {

    private lateinit var binding: FragmentMyPropertyBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_property, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMyPropertyBinding.bind(view)

        // SET ADD PROPERTY BUTTON
        setupListeners()
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