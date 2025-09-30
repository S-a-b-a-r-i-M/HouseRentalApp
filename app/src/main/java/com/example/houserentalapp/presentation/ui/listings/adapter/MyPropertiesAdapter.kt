package com.example.houserentalapp.presentation.ui.listings.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.presentation.enums.PropertyLandlordAction
import com.example.houserentalapp.presentation.model.PropertySummaryUI
import com.example.houserentalapp.presentation.ui.base.BaseDiffCallBack
import com.example.houserentalapp.presentation.ui.base.BaseLoadingAdapter
import com.example.houserentalapp.presentation.ui.base.LoadingAdapterData
import com.example.houserentalapp.presentation.ui.common.PopupMenuBuilder
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.helpers.fromEpoch
import com.example.houserentalapp.presentation.utils.helpers.loadImageSourceToImageView
import com.google.android.material.imageview.ShapeableImageView

sealed class MyPropertiesAdapterData {
    data class Data(val data: PropertySummaryUI) : MyPropertiesAdapterData()
    data class Header(val date: String) : MyPropertiesAdapterData()
}

class MyPropertiesAdapter(
    val onClick: (Long) -> Unit,
    val onPropertyAction: (PropertySummary, PropertyLandlordAction) -> Unit
) : BaseLoadingAdapter<MyPropertiesAdapterData>() {
    inner class DataViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private var imageView: ShapeableImageView = itemView.findViewById(R.id.imgProperty)
        private var tvHeader: TextView = itemView.findViewById(R.id.tvHeader)
        private var tvBody1: TextView = itemView.findViewById(R.id.tvBody1)
        private var tvBody2: TextView = itemView.findViewById(R.id.tvBody2)
        private var tvFooter: TextView = itemView.findViewById(R.id.tvFooter)
        private var tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private var tvAddedDate: TextView = itemView.findViewById(R.id.tvAddedDate)
        private var ibtnAction: ImageButton = itemView.findViewById(R.id.ibtnAction)


        @SuppressLint("UseCompatTextViewDrawableApis")
        fun bind(summaryUI: PropertySummaryUI) {
            val context = itemView.context
            val summary = summaryUI.summary

            // Add images programmatically
            if (summary.images.isNotEmpty()) try {
                loadImageSourceToImageView(summary.images[0].imageSource, imageView)
            } catch (exp: Exception) {
                logError("Error on Add images programmatically exp:${exp.message}")
            }
            else // Add Place Holder Image
                imageView.setImageResource(R.drawable.no_image)

            // Add Details
            tvAddedDate.text = summary.createdAt.fromEpoch()
            tvHeader.text = summary.name
            tvBody1.text = context.getString(
                R.string.property_summary_body1,
                summary.address.city,
                summary.address.locality
            )
            tvBody2.text = context.getString(R.string.property_price, summary.price)
            tvFooter.text = context.getString(
                R.string.property_summary_footer,
                summary.bhk.readable,
                summary.furnishingType.readable,
                summary.builtUpArea
            )
            tvStatus.apply {
                // Initially set Active values
                var textId = R.string.active
                var drawable = R.drawable.outline_check_circle_24
                var colorId = R.color.green_success
                if (!summary.isActive) {
                    textId = R.string.inactive
                    drawable = R.drawable.outline_stop_circle_24
                    colorId = R.color.red_error
                }

                text = context.getString(textId)
                setTextColor(context.getColor(colorId))
                setCompoundDrawablesWithIntrinsicBounds(0, drawable, 0, 0)
                setCompoundDrawablePadding(0)
                compoundDrawableTintList = ColorStateList.valueOf(context.resources.getColor(colorId))
            }

            // Set On Clicks
            imageView.setOnClickListener {
                logInfo("My Property ${summary.id} Clicked")
                onClick(summary.id)
            }
            itemView.setOnClickListener {
                logInfo("My Property ${summary.id} Clicked")
                onClick(summary.id)
            }
            ibtnAction.setOnClickListener {
                showActionsPopupMenu2(ibtnAction, summary)
            }
        }

        private fun showActionsPopupMenu2(anchorView: View, summary: PropertySummary) {
            var option2Title = "Make Inactive"
            var option2Drawable = R.drawable.baseline_visibility_off_24
            if (!summary.isActive) {
                option2Title = "Make Active"
                option2Drawable = R.drawable.baseline_visibility_24
            }
            PopupMenuBuilder(anchorView.context, anchorView)
                .addItem(1, "Edit", icon = R.drawable.baseline_edit_24)
                .addItem(2, option2Title, option2Drawable)
                .addItem(3, "Delete", icon = R.drawable.baseline_delete_24)
                .setOnItemClickListener { option ->
                    when(option.id) {
                        1 -> {
                            onPropertyAction(summary, PropertyLandlordAction.EDIT)
                            true
                        }
                        2 -> {
                            onPropertyAction(summary, PropertyLandlordAction.CHANGE_AVAILABILITY)
                            true
                        }
                        3 -> {
                            onPropertyAction(summary, PropertyLandlordAction.DELETE)
                            true
                        }
                        else -> false
                    }
                }
                .show()
        }
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.tvHeader)

        fun bind(text: String) {
            textView.text = text
        }
    }

    class LoaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {
        const val HEADER_VIEW_TYPE = 3
    }

    override fun createDiffCallBack(
        oldList: List<LoadingAdapterData<MyPropertiesAdapterData>>,
        newList: List<LoadingAdapterData<MyPropertiesAdapterData>>
    ): BaseDiffCallBack<MyPropertiesAdapterData> {
        return MyPropertiesDiffCallBack(oldList, newList)
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = itemList[position]) {
            is LoadingAdapterData.Data<*> -> {
                if (item.data is MyPropertiesAdapterData.Data)
                    DATA_VIEW_TYPE
                else
                    HEADER_VIEW_TYPE
            }
            LoadingAdapterData.Loader -> LOADER_VIEW_TYPE
        }
    }

    override fun onCreateDataViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.my_property_summary_layout,
            parent,
            false
        )
        return DataViewHolder(view)
    }

    override fun onCreateLoaderViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.loader_item,
            parent,
            false
        )
        return LoaderViewHolder(view)
    }

    fun onCreateHeaderViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.header_layout,
            parent,
            false
        )
        return HeaderViewHolder(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            HEADER_VIEW_TYPE -> onCreateHeaderViewHolder(parent)
            DATA_VIEW_TYPE -> onCreateDataViewHolder(parent)
            LOADER_VIEW_TYPE -> onCreateLoaderViewHolder(parent)
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(val item = itemList[position]) {
            is LoadingAdapterData.Data<*> -> {
                if (item.data is MyPropertiesAdapterData.Data)
                    (holder as DataViewHolder).bind(item.data.data)
                else if (item.data is MyPropertiesAdapterData.Header)
                    (holder as HeaderViewHolder).bind(item.data.date)
            }
            LoadingAdapterData.Loader -> { }
        }
    }

    override fun getItemCount() = itemList.size

    fun setPropertySummaryUiList(newSummaryUI: List<PropertySummaryUI>, hasMore: Boolean) {
        val newDataList = groupByDate(newSummaryUI)
        // TODO: Handle only status change, Handle delete property change, single propertySummaryUpdate
        super.setDataList(newDataList, hasMore)
    }

    fun groupByDate(dataList: List<PropertySummaryUI>): List<MyPropertiesAdapterData> {
        return dataList.groupBy { it.summary.createdAt.fromEpoch("MMMM") }
            .flatMap { (month, items) ->
                val dataList = mutableListOf<MyPropertiesAdapterData>()
                dataList.add(MyPropertiesAdapterData.Header(month))
                items.forEach { dataList.add(MyPropertiesAdapterData.Data(it)) }
                dataList
            }
    }
}

class MyPropertiesDiffCallBack(
    oldList: List<LoadingAdapterData<MyPropertiesAdapterData>>,
    newList: List<LoadingAdapterData<MyPropertiesAdapterData>>,
) : BaseDiffCallBack<MyPropertiesAdapterData>(oldList, newList) {
    override fun areDataItemsSame(
        oldData: MyPropertiesAdapterData,
        newData: MyPropertiesAdapterData
    ): Boolean {
        return when {
            oldData is MyPropertiesAdapterData.Header && newData is MyPropertiesAdapterData.Header
                -> oldData.date == newData.date
            oldData is MyPropertiesAdapterData.Data && newData is MyPropertiesAdapterData.Data
                -> oldData.data == newData.data
            else -> false
        }
    }
}