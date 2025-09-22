package com.example.houserentalapp.presentation.utils.helpers

import android.content.Context
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.logWarning

fun setSystemBarBottomPadding(view: View) {
    ViewCompat.setOnApplyWindowInsetsListener(view){ _, insets ->
        val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        Log.d("setSystemBarBottomPadding", "systemBarInsets bottom: ${systemBarInsets.bottom} px")
        view.setPadding(
            view.paddingLeft,
            view.paddingTop,
            view.paddingRight,
            systemBarInsets.bottom
        )

        insets
    }
}

fun getRequiredStyleLabel(label: String, context: Context): SpannableString {
    val spannable = SpannableString("$label*")
    spannable.setSpan(
        ForegroundColorSpan(context.resources.getColor(R.color.red_error)),
        spannable.length - 1,
        spannable.length,
        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE // Both start and end are exclusive
    )
    return spannable
}

fun getScrollListener(hasMore: () -> Boolean, onLoad: () -> Unit) =
    object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            val layoutManger = recyclerView.layoutManager as LinearLayoutManager

            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                return // No need to fetch new items while scrolling
            } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (!hasMore()) return

                val lastVisibleItemPosition =
                    layoutManger.findLastCompletelyVisibleItemPosition() // index
                val totalItemCount = recyclerView.adapter?.itemCount ?: run {
                    logWarning("totalItemCount is not accessible")
                    return
                }
                val shouldLoadMore = (lastVisibleItemPosition + 1) >= totalItemCount
                if (shouldLoadMore) {
                    logInfo("<----------- from onScroll State changed ---------->")
                    onLoad()
                }
            }
        }
    }