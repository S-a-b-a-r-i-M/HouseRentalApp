package com.example.houserentalapp.presentation.ui.property

import android.view.View
import com.google.android.material.button.MaterialButton

data class SelectableMaterialButtonData(
    val button: MaterialButton, val onClickListener: View.OnClickListener? = null
)

// TODO-DOUBT: Will it lead to any kind of memory leaks ?
class SingleSelectableGroupedButtons(buttons: List<SelectableMaterialButtonData>) {

    private var selectedButton: MaterialButton? = null

    init {
        buttons.forEach { (button, onClickListener) ->
            // ADD ONCLICK
            if (onClickListener != null)
                button.setOnClickListener(onClickListener)

            // CHECKED CHANGE
            button.addOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedButton?.let {
                        it.isClickable = true
                        it.isChecked = false
                    }
                    // ASSIGN NEW BUTTON
                    selectedButton = button.apply { isClickable = false }
                }
            }
        }
    }

    fun getSelectedButton() = selectedButton
}