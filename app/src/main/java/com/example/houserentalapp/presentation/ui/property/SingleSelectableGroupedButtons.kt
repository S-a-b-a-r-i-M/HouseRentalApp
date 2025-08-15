package com.example.houserentalapp.presentation.ui.property

import android.util.Log
import android.view.View
import com.example.houserentalapp.presentation.utils.extensions.logDebug
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.google.android.material.button.MaterialButton

data class SelectableMaterialButtonData(
    val button: MaterialButton,
    val onSelect: () -> Unit = { },
    val onDeSelect: () -> Unit = { },
    val onClick: View.OnClickListener? = null,
)

// TODO-DOUBT: Will it lead to any kind of memory leaks ?
class SingleSelectableGroupedButtons(buttonsData: List<SelectableMaterialButtonData>) {

    private var selectedButtonData: SelectableMaterialButtonData? = null

    init {
        buttonsData.forEach {
            // ONCLICK
            it.button.setOnClickListener(it.onClick)

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
                    it.onSelect() // Call Select of new button
                } else
                    it.onDeSelect() // Call Select of UnSelected button
            }
        }
    }

    fun getSelectedButton() = selectedButtonData
}