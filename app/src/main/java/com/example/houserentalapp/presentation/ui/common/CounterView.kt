package com.example.houserentalapp.presentation.ui.common

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.ColorInt
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
            if (value != field) {
                field = value.coerceIn(minCount, maxCount)
                binding.tvCount.text = field.toString()
                onCountChangeListener?.invoke(field)
                updateButtonStatus()
            }
        }

    companion object
    {
        const val DEFAULT_TEXT_SIZE_IN_SP = 14f // in sp
        const val DEFAULT_ICON_SIZE_IN_DP = 26f // in dp
        const val DEFAULT_COLOR = Color.GRAY
    }
    // STYLING PROPERTIES
    var labelStyle: Typeface = Typeface.DEFAULT
        set(value) {
            field = value
            binding.tvLabel.typeface = value
        }

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

    var iconWidth: Float = DEFAULT_ICON_SIZE_IN_DP // size in dp
        set(value) {
            field = value
            updateIconDimensions()
        }

    var iconHeight: Float = DEFAULT_ICON_SIZE_IN_DP // size in dp
        set(value) {
            field = value
            updateIconDimensions()
        }

    @ColorInt
    var labelColor: Int = DEFAULT_COLOR
        set(value) {
            field = value
            binding.tvLabel.setTextColor(value)
        }

    @ColorInt
    var counterColor: Int = DEFAULT_COLOR
        set(value) {
            field = value
            binding.tvCount.setTextColor(value)
        }

    @ColorInt
    var iconTintColor: Int = DEFAULT_COLOR
        set(value) {
            field = value
            binding.btnDecrement.imageTintList = ColorStateList.valueOf(value)
            binding.btnIncrement.imageTintList = ColorStateList.valueOf(value)
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
                val textSizePx = DEFAULT_TEXT_SIZE_IN_SP.spToPX()
                labelSize = getDimensionPixelSize(
                    R.styleable.CounterView_labelSize, textSizePx
                ).pxToSP()
                counterSize = getDimensionPixelSize(
                    R.styleable.CounterView_counterSize, textSizePx
                ).pxToSP()

                // Icon dimensions
                iconWidth = getDimensionPixelSize(
                    R.styleable.CounterView_iconWidth, iconWidth.dpToPX()
                ).pxToDP()
                iconHeight = getDimensionPixelSize(
                    R.styleable.CounterView_iconHeight, iconHeight.dpToPX()
                ).pxToDP()

                // Colors
                labelColor = getColor(R.styleable.CounterView_labelColor, DEFAULT_COLOR)
                counterColor = getColor(R.styleable.CounterView_counterColor, DEFAULT_COLOR)
                iconTintColor = getColor(R.styleable.CounterView_iconTintColor, DEFAULT_COLOR)
            } finally {
                recycle()
            }
        }

        setClickListeners()
        updateButtonStatus()
    }

    // Save Into State
    override fun onSaveInstanceState(): Parcelable? {
        return Bundle().apply {
            // Save Parent Sate
            putParcelable("superState", super.onSaveInstanceState())
            // Save Our Class States
            putInt("count", count)
        }
    }

    // Restore From State
    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            // Parse Our Class State
            count = state.getInt("count")
            // Parse Parent State
            super.onRestoreInstanceState(state.getParcelable("superState"))
        }
        else
            super.onRestoreInstanceState(state)
    }

    private fun updateIconDimensions() {
        val widthPx = iconWidth.dpToPX()
        val heightPx = iconHeight.dpToPX()

        binding.btnDecrement.updateLayoutParams {
            width = widthPx
            height = heightPx
        }
        binding.btnIncrement.updateLayoutParams {
            width = widthPx
            height = heightPx
        }
    }

    private fun updateButtonStatus() {
        with(binding) {
            if (count == minCount) {
                btnDecrement.apply {
                    isEnabled = false
                    alpha = 0.2f
                    isClickable = false
                }
            } else if (count > minCount) {
                btnDecrement.apply {
                    isEnabled = true
                    alpha = 1f
                    isClickable = true
                }
            }

            if (count == maxCount) {
                btnIncrement.apply {
                    isEnabled = false
                    alpha = 0.2f
                    isClickable = false
                }
            } else if (count < maxCount) {
                btnIncrement.apply {
                    isEnabled = true
                    alpha = 1f
                    isClickable = true
                }
            }
        }
    }

    // CLICK LISTENERS
    private fun setClickListeners() {
        binding.btnIncrement.setOnClickListener {
            if (count < maxCount) {
                ++count
                onCountIncrementListener?.invoke()
            }
        }

        binding.btnDecrement.setOnClickListener {
            if (count > minCount) {
                --count
                onCountDecrementListener?.invoke()
            }
        }
    }

    // PUBLIC METHODS
    fun setRange(min: Int, max: Int) {
        minCount = min
        maxCount = max
        count = count.coerceIn(minCount, maxCount)
    }

    fun setIconDimensions(widthInDp: Float, heightInDp: Float) {
        iconWidth = widthInDp
        iconHeight = heightInDp
    }

    // HELPER FUNCTIONS
    private fun Float.dpToPX(): Int = (this * context.resources.displayMetrics.density).toInt()
    private fun Int.pxToDP(): Float = this / context.resources.displayMetrics.density
    private fun Float.spToPX(): Int = (this * context.resources.displayMetrics.density).toInt()
    private fun Int.pxToSP(): Float = this / context.resources.displayMetrics.density
}