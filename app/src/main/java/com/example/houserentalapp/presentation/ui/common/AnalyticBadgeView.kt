package com.example.houserentalapp.presentation.ui.common

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import com.example.houserentalapp.R
import com.example.houserentalapp.databinding.AnalyticCardBinding
import com.example.houserentalapp.presentation.utils.extensions.logWarning

class AnalyticBadgeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?= null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private var binding = AnalyticCardBinding
        .inflate(LayoutInflater.from(context), this, true)

    var onItemClick: () -> Unit = { }

    init {
        // Read custom attributes
        context.obtainStyledAttributes(attrs, R.styleable.AnalyticBadgeView).apply {
            try {
                // Set icon
                val iconRes = getResourceId(R.styleable.AnalyticBadgeView_iconSrc, 0)
                val iconTint = getColorStateList(R.styleable.AnalyticBadgeView_iconTint)
                if (iconRes != 0) setIcon(iconRes, iconTint)

                // Set Title
                val title = getString(R.styleable.AnalyticBadgeView_title)
                if (!title.isNullOrEmpty()) setTitle(title)

                // Set SubTitle
                val subTitle = getString(R.styleable.AnalyticBadgeView_subTitle)
                if (!subTitle.isNullOrEmpty()) setSubTitle(subTitle)

                // Set Badge Count
                setBadgeCount(getInt(R.styleable.AnalyticBadgeView_badgeCount, 0))
            } finally {
                recycle()
            }
        }

        setupListener()
    }

    fun setIcon(@DrawableRes iconRes: Int, colorStateList: ColorStateList?) {
        binding.imgIcon.apply {
            setImageResource(iconRes)
            if (colorStateList != null) imageTintList = colorStateList
        }
    }

    fun setTitle(title: String) {
        binding.tvTitle.text = title
    }

    fun setSubTitle(subTitle: String) {
        binding.tvSubTitle.text = subTitle
    }

    fun setBadgeCount(count: Int) {
        if (count < 0) {
            logWarning("badge count can't be negative")
            return
        }
        binding.badge.apply {
            if(count > 999) {
                text = context.getString(R.string._999)
                textSize = 9.5f
            }
            else {
                text = count.toString()
                textSize = 11f
            }
        }
    }

    fun hideBadge() {
        binding.badge.visibility = View.GONE
    }

    fun showBadge() {
        binding.badge.visibility = View.VISIBLE
    }

    private fun setupListener() {
        binding.llAnalyticCard.setOnClickListener {
            onItemClick()
        }
    }
}