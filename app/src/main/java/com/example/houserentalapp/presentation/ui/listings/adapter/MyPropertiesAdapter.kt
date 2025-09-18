package com.example.houserentalapp.presentation.ui.listings.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.presentation.enums.PropertyLandlordAction
import com.example.houserentalapp.presentation.model.PropertySummaryUI
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.helpers.fromEpoch
import com.example.houserentalapp.presentation.utils.helpers.loadImageSourceToImageView
import com.google.android.material.imageview.ShapeableImageView

sealed class DataList {
    data class Data(val data: PropertySummaryUI) : DataList()
    data class Header(val date: String) : DataList()
}

class MyPropertiesAdapter(
    val onClick: (Long) -> Unit,
    val onPropertyAction: (PropertySummary, PropertyLandlordAction) -> Unit
)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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
                imageView.setImageResource(R.drawable.room_1)

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
                showActionsPopupMenu(ibtnAction, summary)
            }
        }

        private fun showActionsPopupMenu(anchorView: View, summary: PropertySummary) {
            val popupMenu = PopupMenu(anchorView.context, anchorView, Gravity.START)
            popupMenu.menuInflater.inflate(R.menu.property_action_menu, popupMenu.menu)
            popupMenu.menu.findItem(R.id.action_change_availability).title = if (summary.isActive)
                "Make Inactive"
            else
                "Make Active"
            popupMenu.setOnMenuItemClickListener { item ->
                when(item.itemId) {
                    R.id.action_edit -> {
                        onPropertyAction(summary, PropertyLandlordAction.EDIT)
                        true
                    }
                    R.id.action_change_availability -> {
                        onPropertyAction(summary, PropertyLandlordAction.CHANGE_AVAILABILITY)
                        true
                    }
                    R.id.action_delete -> {
                        onPropertyAction(summary, PropertyLandlordAction.DELETE)
                        true
                    }
                    else -> false
                }
            }

            // popupMenu.setForceShowIcon(true) // Not Working
            popupMenu.show()
        }
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.tvHeader)

        fun bind(text: String) {
            textView.text = text
        }
    }

    companion object {
        const val TYPE_HEADER = 1
        const val TYPE_DATA = 2
    }

    private var dataList: MutableList<DataList> = mutableListOf()

    override fun getItemViewType(position: Int) = when(dataList[position]) {
        is DataList.Header -> TYPE_HEADER
        is DataList.Data -> TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        println("---------------- onCreateViewHolder -----------------")
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            TYPE_HEADER -> {
                val view = inflater
                    .inflate(R.layout.header_layout, parent, false)
                HeaderViewHolder(view)
            }
            TYPE_DATA -> {
                val view = inflater
                    .inflate(R.layout.my_property_summary_layout2, parent, false)
                DataViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(val data = dataList[position]) {
            is DataList.Data -> (holder as DataViewHolder).bind(data.data)
            is DataList.Header -> (holder as HeaderViewHolder).bind(data.date)
        }
    }

    override fun getItemCount() = dataList.size

    fun setDataList(newSummaryUI: List<PropertySummaryUI>) {
        val newDataList = groupByDate(newSummaryUI)
        // TODO: Handle only status change, Handle delete property change
        val diffCallBack = MyPropertiesDiffCallBack(dataList, newDataList)
        val diffResult = DiffUtil.calculateDiff(diffCallBack)

        dataList.clear()
        dataList.addAll(newDataList)
        diffResult.dispatchUpdatesTo(this)
    }

    fun groupByDate(dataList: List<PropertySummaryUI>): List<DataList> {
        return dataList.groupBy { it.summary.createdAt.fromEpoch("MMMM") }
            .flatMap { (month, items) ->
                val dataList = mutableListOf<DataList>()
                dataList.add(DataList.Header(month))
                items.forEach { dataList.add(DataList.Data(it)) }
                dataList
            }
    }
}

class MyPropertiesDiffCallBack(
    private val oldList: List<DataList>,
    private val newList: List<DataList>,
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]


        return if (oldItem::class == newItem::class) {
            when(oldItem) {
                is DataList.Header -> oldItem.date == (newItem as DataList.Header).date
                is DataList.Data ->
                    oldItem.data.summary.id == (newItem as DataList.Data).data.summary.id
            }
        } else
            false
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return when(oldItem) {
            is DataList.Header -> oldItem.date == (newItem as DataList.Header).date
            is DataList.Data -> oldItem.data == (newItem as DataList.Data).data
        }
    }
}