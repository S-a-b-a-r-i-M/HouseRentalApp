package com.example.houserentalapp.presentation.ui.listings.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.domain.model.enums.LeadStatus
import com.example.houserentalapp.presentation.ui.common.CustomPopupMenu
import com.example.houserentalapp.presentation.ui.common.MenuOption
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.logWarning
import com.example.houserentalapp.presentation.utils.helpers.fromEpoch
import com.example.houserentalapp.presentation.utils.helpers.loadImageSourceToImageView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView

class LeadInterestedPropertiesAdapter(private val onLeadStatusChange: (Long, LeadStatus) -> Unit)
    : RecyclerView.Adapter<LeadInterestedPropertiesAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private var imageView: ShapeableImageView = itemView.findViewById(R.id.imgProperty)
        private var tvAddedDate: TextView = itemView.findViewById(R.id.tvAddedDate)
        private var btnLeadStatus = itemView.findViewById<MaterialButton>(R.id.btnLeadStatus)
        private var tvHeader: TextView = itemView.findViewById(R.id.tvHeader)
        private var tvBody1: TextView = itemView.findViewById(R.id.tvBody1)
        private var tvBody2: TextView = itemView.findViewById(R.id.tvBody2)
        private var tvFooter: TextView = itemView.findViewById(R.id.tvFooter)
        private var tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        @SuppressLint("UseCompatTextViewDrawableApis")
        fun bind(summaryWithLeadStatus: Pair<PropertySummary, LeadStatus>) {
            val context = itemView.context
            val (summary, status) = summaryWithLeadStatus

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

            // Lead Status
            btnLeadStatus.text = status.readable
            btnLeadStatus.setOnClickListener { showCustomMenu(it, summary.id) }
        }

        fun bindOnlyStatus(newStatus: LeadStatus) {
            btnLeadStatus.text = newStatus.readable
        }

        private fun showCustomMenu(view: View, propertyId: Long, currentStatus: LeadStatus? = null) {
            val menuOptions = LeadStatus.entries.map {
                MenuOption(id = it.ordinal, title = it.readable)
            }
            val popupMenu = CustomPopupMenu(view.context, view)
            popupMenu.setOnItemClickListener { option ->
                statusChangeTriggeredPropertyId = propertyId
                onLeadStatusChange(propertyId, LeadStatus.values[option.id])
            }
            popupMenu.show(menuOptions)
        }
    }

    private var statusChangeTriggeredPropertyId: Long? = null
    private var dataList = mutableListOf<Pair<PropertySummary, LeadStatus>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.interested_property_summary_layout, parent, false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: List<Any?>) {
        if (payloads.isNotEmpty() && payloads[0] == STATUS_CHANGE) {
            val newStatus = dataList[position].second
            holder.bindOnlyStatus(newStatus)
            return
        }

        onBindViewHolder(holder, position)
    }

    override fun getItemCount() = dataList.size

    fun setDataList(newSummaries: List<Pair<PropertySummary, LeadStatus>>) {
        // Check Is this Triggered By Status Change
        if (statusChangeTriggeredPropertyId != null && newSummaries.size == dataList.size) {
            val index = dataList.indexOfFirst { it.first.id == statusChangeTriggeredPropertyId }
            if (index != -1) {
                logInfo("setDataList statusChangeTriggeredPropertyId received ")
                dataList[index] = newSummaries[index]
                statusChangeTriggeredPropertyId = null // Make it null
                notifyItemChanged(index, STATUS_CHANGE)
            } else
                logWarning("statusChangeTriggeredPropertyId is not found in data list")
        }

        val diffCallBack = LeadInterestedPropertiesDiffCallBack(dataList, newSummaries)
        val diffResult = DiffUtil.calculateDiff(diffCallBack)

        dataList.clear()
        dataList.addAll(newSummaries)
        diffResult.dispatchUpdatesTo(this)
    }

    companion object {
        const val STATUS_CHANGE = "STATUS_CHANGE"
    }
}

class LeadInterestedPropertiesDiffCallBack(
    private val oldList: List<Pair<PropertySummary, LeadStatus>>,
    private val newList: List<Pair<PropertySummary, LeadStatus>>,
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].first.id == newList[newItemPosition].first.id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].second == newList[newItemPosition].second
    }
}