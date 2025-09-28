package com.example.houserentalapp.presentation.ui.common

import android.content.Context
import android.content.res.Resources
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.example.houserentalapp.R

// 1. Data class for menu items
data class MenuOption(
    val id: Int,
    val title: String,
    val icon: Int? = null,
    val isEnabled: Boolean = true,
    val textColor: Int? = null,
    val isDividerAfter: Boolean = false
)

// 2. Custom PopupMenu class
class CustomPopupMenu(private val context: Context, private val anchorView: View) {
    private var popupWindow: PopupWindow? = null
    private var onItemClickListener: ((MenuOption) -> Unit)? = null

    fun setOnItemClickListener(listener: (MenuOption) -> Unit) {
        onItemClickListener = listener
    }

    fun show(menuOptions: List<MenuOption>) {
        if (menuOptions.isEmpty()) return

        val popupView = LayoutInflater.from(context)
            .inflate(R.layout.custom_popup_menu, null)
        val menuContainer = popupView.findViewById<LinearLayout>(R.id.menuContainer)

        // Add menu items
        menuOptions.forEach { option ->
            addMenuItem(menuContainer, option)

            // Add divider if needed
            if (option.isDividerAfter && option != menuOptions.last())
                addDivider(menuContainer)
        }

        // Create and configure popup window
        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            elevation = 8f
            // setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.popup_background))
            isOutsideTouchable = true
            isFocusable = true
        }

        // Show popup
        showPopup()
    }

    private fun addMenuItem(container: LinearLayout, option: MenuOption) {
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.popup_menu_item, container, false)

        val iconView = itemView.findViewById<ImageView>(R.id.menuIcon)
        val titleView = itemView.findViewById<TextView>(R.id.menuTitle)

        // Set icon
        if (option.icon != null) {
            iconView.visibility = View.VISIBLE
            iconView.setImageResource(option.icon)
        } else
            iconView.visibility = View.GONE

        // Set title
        titleView.text = option.title

        // Set text color
        option.textColor?.let { color ->
            titleView.setTextColor(ContextCompat.getColor(context, color))
        }

        // Set click listener
        itemView.setOnClickListener {
            onItemClickListener?.invoke(option)
            dismiss()
        }

        container.addView(itemView)
    }

    private fun addDivider(container: LinearLayout) {
        val divider = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.dp
            ).apply {
                setMargins(16.dp, 4.dp, 16.dp, 4.dp)
            }
            setBackgroundColor(ContextCompat.getColor(context, R.color.gray_medium))
        }
        container.addView(divider)
    }

    private fun showPopup() {
        // Calculate position
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        val anchorX = location[0]
        val anchorY = location[1]
        val anchorWidth = anchorView.width
        val anchorHeight = anchorView.height

        // Measure popup size
        popupWindow?.contentView?.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED
        )

        val popupWidth = popupWindow?.contentView?.measuredWidth ?: 0
        val popupHeight = popupWindow?.contentView?.measuredHeight ?: 0

        // Calculate position (show below anchor by default)
        var x = anchorX
        var y = anchorY + anchorHeight + 8.dp

        // Adjust if popup goes off screen
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        // Adjust horizontal position
         if (x + popupWidth > screenWidth) x = screenWidth - popupWidth - 16.dp
        // Adjust vertical position
        if (y + popupHeight > screenHeight) y = anchorY - popupHeight - 8.dp

        popupWindow?.showAtLocation(anchorView, Gravity.NO_GRAVITY, x, y)
    }

    fun dismiss() {
        popupWindow?.dismiss()
        popupWindow = null
    }

    fun isShowing(): Boolean = popupWindow?.isShowing == true
}

// Extension property for dp conversion
val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

// Builder pattern for easier usage
class PopupMenuBuilder(private val context: Context, private val anchorView: View) {
    private val menuOptions = mutableListOf<MenuOption>()
    private var onItemClickListener: ((MenuOption) -> Unit)? = null

    fun addItem(
        id: Int,
        title: String,
        @DrawableRes icon: Int? = null,
        isEnabled: Boolean = true,
        textColor: Int? = null,
        isDividerAfter: Boolean = false
    ): PopupMenuBuilder {
        menuOptions.add(MenuOption(id, title, icon, isEnabled, textColor, isDividerAfter))
        return this
    }

    fun addDivider(): PopupMenuBuilder {
        if (menuOptions.isNotEmpty())
            menuOptions[menuOptions.size - 1] = menuOptions.last().copy(isDividerAfter = true)

        return this
    }

    fun setOnItemClickListener(listener: (MenuOption) -> Unit): PopupMenuBuilder {
        onItemClickListener = listener
        return this
    }

    fun show() {
        val popupMenu = CustomPopupMenu(context, anchorView)
        onItemClickListener?.let { popupMenu.setOnItemClickListener(it) }
        popupMenu.show(menuOptions)
    }
}