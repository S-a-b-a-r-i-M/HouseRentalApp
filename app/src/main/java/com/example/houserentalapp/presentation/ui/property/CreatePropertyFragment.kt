package com.example.houserentalapp.presentation.ui.property

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.FragmentCreatePropertyBinding

class CreatePropertyFragment : Fragment() {

    private lateinit var binding: FragmentCreatePropertyBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_property, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCreatePropertyBinding.bind(view)

        // Enable Tool Bar
        setCustomToolBar()
    }

    private fun setCustomToolBar() {
        with(binding) {
            titleTV.text = getString(R.string.create_property)

            backImgBtn.setOnClickListener {
                (requireActivity() as AppCompatActivity).onBackPressedDispatcher.onBackPressed()
            }
        }
    }
}