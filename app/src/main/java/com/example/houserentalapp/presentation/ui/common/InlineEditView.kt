package com.example.houserentalapp.presentation.ui.common

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.InlineEditTextViewBinding

class InlineEditView @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val binding = InlineEditTextViewBinding
        .inflate(LayoutInflater.from(context), this, true)
    private var isInEditMode = false
    private var originalValue = ""

    // Callbacks
    var onValueChanged: ((String) -> Unit)? = null

    init {
        context.obtainStyledAttributes(attrs, R.styleable.InlineEditView).apply {
            try {
                with(binding) {
                    // Basic properties
                    tvLabel.apply {
                        text = getString(R.styleable.InlineEditView_label) ?: ""
                        textSize = getDimension(R.styleable.InlineEditView_labelSize, 14f)
                    }
                }
            } finally {
                recycle()
            }
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        with(binding) {
            ibtnEdit.setOnClickListener {
                enterEditMode()
            }

            ibtnSave.setOnClickListener {
                onValueChanged?.invoke(etValue.text.toString())
                exitEditMode()
            }

            ibtnCancel.setOnClickListener {
                exitEditMode()
            }
        }
    }

    private fun enterEditMode() {
        if (isInEditMode) return
        with(binding) {
            isInEditMode = true
            originalValue = tvValue.text.toString()

            tvValue.visibility = View.GONE
            ibtnEdit.visibility = View.GONE

            etValue.visibility = View.VISIBLE
            ibtnSave.visibility = View.VISIBLE
            ibtnCancel.visibility = View.VISIBLE

            etValue.setText(tvValue.text)
            etValue.requestFocus()
            showKeyboard()
        }
    }

    private fun exitEditMode() {
        if (!isInEditMode) return
        with(binding) {
            isInEditMode = false

            etValue.visibility = View.GONE
            ibtnSave.visibility = View.GONE
            ibtnCancel.visibility = View.GONE

            tvValue.visibility = View.VISIBLE
            ibtnEdit.visibility = View.VISIBLE

            hideKeyBoard()
        }
    }

    private fun showKeyboard() {
        binding.etValue.postDelayed({
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etValue, InputMethodManager.SHOW_IMPLICIT)

        }, 100)
    }

    private fun hideKeyBoard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etValue.windowToken, 0)
    }

    // Public methods for external control
    fun setLabel(label: String) {
        binding.tvLabel.text = label
    }

    fun setValue(value: String) {
        if (isInEditMode)
            binding.etValue.setText(value)
        else
            binding.tvValue.text = value
    }

    fun getValue(): String {
        return if (isInEditMode)
            binding.etValue.text.toString()
        else
            binding.tvValue.text.toString()
    }

    // Save/restore state for configuration changes
    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable("superState", super.onSaveInstanceState())
            putBoolean("isInEditMode", isInEditMode)
            putString("originalValue", originalValue)
            putString("currentValue", getValue())
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            val wasInEditMode = state.getBoolean("isInEditMode")
            originalValue = state.getString("originalValue", "")
            val currentValue = state.getString("currentValue", "")

            super.onRestoreInstanceState(state.getParcelable("superState"))

            setValue(currentValue)

            if (wasInEditMode)
                post {
                    enterEditMode()
                    setValue(currentValue)
                } // TODO-DOUBT
        } else
            super.onRestoreInstanceState(state)
    }
}