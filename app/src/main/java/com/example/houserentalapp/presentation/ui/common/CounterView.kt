package com.example.houserentalapp.presentation.ui.common

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.ViewCounterBinding


/*
<com.example.houserentalapp.presentation.ui.reusable.CounterView
        android:id="@+id/bedroomsCounter"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        app:label="bedrooms"
        app:count="10"
        app:minCount="1"
        app:maxCount="8"
        app:labelSize="18sp"
        app:counterSize="20sp"
        app:iconWidth="30dp"
        app:iconHeight="30dp"
        app:labelColor="@color/primary_blue"
        app:counterColor="@color/primary_blue"
        app:iconTintColor="@color/primary_blue" />
 */

/* TODO
    1. View Counter Color and touch target
    2.
*/

class CounterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr) {
    // PROPERTIES
    private var binding: ViewCounterBinding = ViewCounterBinding
        .inflate(LayoutInflater.from(context), this)

    var label: String = ""
        set(value) {
            field = value
            binding.tvLabel.text = value
        }
    var minCount = Int.MIN_VALUE
    var maxCount = Int.MAX_VALUE
    var count: Int = 0
        set(value) {
            field = value.coerceIn(minCount, maxCount)
            binding.tvCount.text = field.toString()
            onCountChangeListener?.invoke(field)
            updateButtonStatus()
        }

    // STYLING PROPERTIES
    val DEFAULT_TEXT_SIZE_IN_SP = 14f // in sp
    val DEFAULT_TEXT_SIZE_IN_PX = DEFAULT_TEXT_SIZE_IN_SP.toInt().spToPx() // in px
    val DEFAULT_ICON_SIZE_IN_PX = 25.dpToPx() // in px
    val DEFAULT_COLOR = R.color.gray_dark

    var labelSize: Float = DEFAULT_TEXT_SIZE_IN_SP // size in Sp
        set(value) {
            field = value
            binding.tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
        }

    var counterSize: Float = DEFAULT_TEXT_SIZE_IN_SP // size in sp
        set(value) {
            field = value
            binding.tvCount.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
        }

    var iconWidth: Int = DEFAULT_ICON_SIZE_IN_PX // size in px
        set(value) {
            field = value
            updateIconDimensions()
        }

    var iconHeight: Int = DEFAULT_ICON_SIZE_IN_PX // size in px
        set(value) {
            field = value
            updateIconDimensions()
        }

    var labelColor: Int = DEFAULT_COLOR
        set(value) {
            field = value
            binding.tvLabel.setTextColor(value)
        }

    var counterColor: Int = DEFAULT_COLOR
        set(value) {
            field = value
            binding.tvCount.setTextColor(value)
        }

    var iconTintColor: Int = DEFAULT_COLOR
        set(value) {
            field = value
            binding.btnDecrement.backgroundTintList = ColorStateList.valueOf(value)
            binding.btnIncrement.backgroundTintList = ColorStateList.valueOf(value)
        }

    // CLICK LISTENERS
    var onCountChangeListener: ((Int) -> Unit)? = null
    var onCountIncrementListener: (() -> Unit)? = null
    var onCountDecrementListener: (() -> Unit)? = null

    // INIT
    init {
        // Set orientation
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_HORIZONTAL

        // Read custom attributes
        context.obtainStyledAttributes(attrs, R.styleable.CounterView).apply {
            try {
                // Basic properties
                label = getString(R.styleable.CounterView_label) ?: ""
                count = getInt(R.styleable.CounterView_count, 0)
                minCount = getInt(R.styleable.CounterView_minCount, 0)
                maxCount = getInt(R.styleable.CounterView_maxCount, maxCount)

                // Text sizes (convert from px to sp)
                labelSize = getDimensionPixelSize(
                    R.styleable.CounterView_labelSize, DEFAULT_TEXT_SIZE_IN_PX
                ).pxToSP()
                counterSize = getDimensionPixelSize(
                    R.styleable.CounterView_counterSize, DEFAULT_TEXT_SIZE_IN_PX
                ).pxToSP()

                // Icon dimensions
                iconWidth = getDimensionPixelSize(
                    R.styleable.CounterView_iconWidth, iconWidth
                )
                iconHeight = getDimensionPixelSize(
                    R.styleable.CounterView_iconHeight, iconHeight
                )

                // Colors
                labelColor = getColor(R.styleable.CounterView_labelColor, DEFAULT_COLOR)
                counterColor = getColor(R.styleable.CounterView_counterColor, DEFAULT_COLOR)
                iconTintColor = getColor(R.styleable.CounterView_iconTintColor, DEFAULT_COLOR)
            } finally {
                recycle()
            }
        }

        setClickListeners()
    }

    private fun updateIconDimensions() {
        binding.btnDecrement.updateLayoutParams {
            width = iconWidth
            height = iconHeight
        }
        binding.btnIncrement.updateLayoutParams {
            width = iconWidth
            height = iconHeight
        }
    }

    private fun updateButtonStatus() {
        with(binding) {
            if (count == minCount) {
                btnDecrement.isEnabled = false
                btnDecrement.alpha = 0.5f
                btnDecrement.isClickable = false
            } else if (count == minCount + 1) {
                btnDecrement.isEnabled = true
                btnDecrement.alpha = 1f
                btnDecrement.isClickable = true
            }

            if (count == maxCount) {
                btnIncrement.isEnabled = false
                btnIncrement.alpha = 0.5f
                btnIncrement.isClickable = false
            } else if (count == maxCount - 1) {
                btnIncrement.isEnabled = true
                btnIncrement.alpha = 1f
                btnIncrement.isClickable = true
            }
        }
    }

    // SET CLICK LISTENERS
    private fun setClickListeners() {
        binding.btnIncrement.setOnClickListener {
            if (count < maxCount)
                onCountIncrementListener?.invoke()
        }

        binding.btnDecrement.setOnClickListener {
            if (count > minCount)
                onCountDecrementListener?.invoke()
        }
    }

    // PUBLIC METHODS
    fun reset() {
        count = 0
    }

    fun setRange(min: Int, max: Int) {
        minCount = min
        maxCount = max
        count = count.coerceIn(minCount, maxCount)
    }

    fun setIconDimensions(widthInDp: Int, heightInDp: Int) {
        iconWidth = widthInDp.dpToPx()
        iconHeight = heightInDp.dpToPx()
    }

    // TODO: Convenience methods for common styling (large, medium, small)


    // HELPER FUNCTIONS
    private fun Int.dpToPx(): Int = (this * context.resources.displayMetrics.density).toInt()
    private fun Int.spToPx(): Int = (this * context.resources.displayMetrics.scaledDensity).toInt()
    private fun Int.pxToSP(): Float = this / context.resources.displayMetrics.scaledDensity
}