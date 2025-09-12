package com.example.houserentalapp.presentation.ui.listings.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R
import com.example.houserentalapp.domain.model.ImageSource
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.presentation.enums.PropertyLandlordAction
import com.example.houserentalapp.presentation.model.PropertySummaryUI
import com.example.houserentalapp.presentation.ui.property.adapter.PropertiesDiffCallBack
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.logWarning
import com.example.houserentalapp.presentation.utils.helpers.fromEpoch
import java.io.File

class MyPropertiesAdapter(
    val onClick: (Long) -> Unit,
    val onPropertyAction: (PropertySummary, PropertyLandlordAction) -> Unit
)
    : RecyclerView.Adapter<MyPropertiesAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
//        private var imageContainer: LinearLayout = itemView.findViewById(R.id.imageContainer)
        private var imageView: ImageView = itemView.findViewById(R.id.imgProperty)
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
                val image = summary.images[0]
                if (image.isPrimary)
                    when(image.imageSource) {
                        is ImageSource.LocalFile -> {
                            val file = File(image.imageSource.filePath)
                            if (!file.exists())
                                logWarning(
                                    "Image(${image.imageSource.filePath}) is not exists, property:${summary.id}"
                                )
                            else
                                imageView.setImageBitmap(
                                    BitmapFactory.decodeFile(file.absolutePath)
                                )
                        }
                        is ImageSource.Uri -> {
                            imageView.setImageURI(image.imageSource.uri)
                        }
                    }
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

    private var dataList: MutableList<PropertySummaryUI> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_property_summary_layout2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount() = dataList.size

    fun setDataList(newDataList: List<PropertySummaryUI>) {
        // TODO: Handle only status change, Handle delete property change
        val diffCallBack = PropertiesDiffCallBack(dataList, newDataList)
        val diffResult = DiffUtil.calculateDiff(diffCallBack)

        dataList.clear()
        dataList.addAll(newDataList)
        diffResult.dispatchUpdatesTo(this)
    }

    fun appendDataList(newDataList: List<PropertySummaryUI>) {
        val startPosition = newDataList.size
        dataList.addAll(newDataList)
        notifyItemRangeInserted(startPosition ,newDataList.size)
    }
}