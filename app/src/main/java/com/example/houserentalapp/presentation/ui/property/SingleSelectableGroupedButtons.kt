package com.example.houserentalapp.presentation.ui.property

import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.google.android.material.button.MaterialButton

data class SelectableButtonData(
    val button: MaterialButton,
    val onSelectChange: (Boolean) -> Unit = { }
)

// TODO-DOUBT: Will it lead to any kind of memory leaks ?
class SingleSelectableGroupedButtons(buttonsData: List<SelectableButtonData>) {

    private var selectedButtonData: SelectableButtonData? = null
    private var onOptionSelectedListener: ((SelectableButtonData) -> Unit)? = null

    fun setOnOptionSelectedListener(onSelect: (SelectableButtonData) -> Unit) {
        this.onOptionSelectedListener = onSelect
    }

    init {
        buttonsData.forEach {
            // CHECKED CHANGE
            it.button.addOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    logDebug("Button: ${it.button.text} is selected")

                    selectedButtonData?.let { oldButton ->
                        with(oldButton) {
                            button.isClickable = true
                            button.isChecked = false
                        }
                        logDebug("OldButton: ${oldButton.button.text} is unselected")
                    }
                    // ASSIGN NEW BUTTON DATA
                    selectedButtonData = it.apply { button.isClickable = false }
                    onOptionSelectedListener?.invoke(it) // Common select handler
                }

                it.onSelectChange(isChecked) // Call Select change of the button
            }
        }
    }

    fun getSelectedButton() = selectedButtonData
}