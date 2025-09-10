package com.example.houserentalapp.presentation.ui.property.adapter

import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R
import com.example.houserentalapp.domain.model.ImageSource
import com.example.houserentalapp.presentation.model.PropertySummaryUI
import com.example.houserentalapp.presentation.utils.extensions.getShapableImageView
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.logWarning
import java.io.File

// TODO: Fix Image iteration
class PropertiesAdapter(val onClick: (Long) -> Unit, val onShortlistToggle: ((Long) -> Unit)? = null)
    : RecyclerView.Adapter<PropertiesAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private var imageContainer: LinearLayout = itemView.findViewById(R.id.imageContainer)
        private var tvHeader: TextView = itemView.findViewById(R.id.tvHeader)
        private var tvBody1: TextView = itemView.findViewById(R.id.tvBody1)
        private var tvBody2: TextView = itemView.findViewById(R.id.tvBody2)
        private var tvFooter: TextView = itemView.findViewById(R.id.tvFooter)
        private var ibtnShortlist: ImageButton = itemView.findViewById(R.id.ibtnShortlist)

        fun bind(summaryUI: PropertySummaryUI) {
            val context = itemView.context
            // Calc. Image Width Based On Screen Width Pixels
            val screenWidth = context.resources.displayMetrics.widthPixels
            val imageWidth = (screenWidth / 2.2).toInt()
            val summary = summaryUI.summary

            // Add images programmatically
            imageContainer.removeAllViews() // Remove the existing images (scenario: reusing views)
            if (summary.images.isNotEmpty())
                summary.images.forEach {
                    try {
                        val shapableImageView = context.getShapableImageView(imageWidth)
                        // Load Image
                        when(it.imageSource) {
                            is ImageSource.LocalFile -> {
                                val file = File(it.imageSource.filePath)
                                if (!file.exists()) {
                                    logWarning(
                                        "Image(${it.imageSource.filePath}) is not exists, property:${summary.id}"
                                    )
                                    return@forEach
                                }
                                shapableImageView.setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))
                            }
                            is ImageSource.Uri -> {
                                shapableImageView.setImageURI(it.imageSource.uri)
                            }
                        }
                        imageContainer.addView(shapableImageView)
                        // TODO: Need to check this
                       // Glide.with(itemView).load(file).into(shapableImageView)
                    } catch (exp: Exception) {
                        logError("Error on Add images programmatically exp:${exp.message}")
                    }
                }
            else // Add Place Holder Image
                repeat(2) {
                val shapableImageView = itemView.context.getShapableImageView(imageWidth)
                shapableImageView.setImageResource(
                    listOf(R.drawable.interior, R.drawable.room_1).random()
                )
                imageContainer.addView(shapableImageView)
            }

            tvHeader.text = context.getString(
                R.string.property_summary_header,
                summary.name,
                summary.bhk.readable
            )
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
            bindShortlistData(summaryUI.isShortListed)

            // Set On Click
            imageContainer.setOnClickListener {
                logInfo("Property ${summary.id} Clicked")
                onClick(summary.id)
            }
            itemView.setOnClickListener {
                logInfo("Property ${summary.id} Clicked")
                onClick(summary.id)
            }
            ibtnShortlist.setOnClickListener {
                if(onShortlistToggle == null) return@setOnClickListener

                it.isEnabled = false // Disable immediately
                shortlistToggledPropertyId = summary.id
                onShortlistToggle(summary.id)

                // Re-enable after delay
                it.postDelayed({it.isEnabled = true}, 1000)
            }
        }


        fun bindShortlistData(isShortListed: Boolean) {
            var colorId = R.color.primary_blue
            if (isShortListed) {
                // Heart beat animation for shortlisted
                ibtnShortlist.animate()
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .rotationX(30f)
                    .setDuration(150)
                    .withEndAction {
                        ibtnShortlist.animate()
                            .scaleX(1.1f)
                            .scaleY(1.1f)
                            .rotationX(0f)
                            .setDuration(100)
                            .start()
                    }
                    .start()
            } else {
                // Simple scale back to normal
                ibtnShortlist.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start()

                colorId = R.color.gray_medium
            }

            ibtnShortlist.imageTintList = ColorStateList.valueOf(
                itemView.context.getColor(colorId)
            )
        }
    }

    private var dataList: MutableList<PropertySummaryUI> = mutableListOf()
    private var shortlistToggledPropertyId: Long? = null // TODO: Make this as index

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_property_summary_layout, parent, false)
        logInfo("<-------- onCreateViewHolder --------> ")
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
        logInfo("<------- onBindViewHolder --------> ")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: List<Any?>) {
        if (payloads.isNotEmpty() && SHORTLIST == payloads[0]) {
            holder.bindShortlistData(dataList[position].isShortListed)
            return
        }

        onBindViewHolder(holder, position) // FallBack
    }

    override fun getItemCount() = dataList.size

    fun setDataList(newDataList: List<PropertySummaryUI>) {
        if (shortlistToggledPropertyId != null && newDataList.size == dataList.size) {
            val oldListIdx = dataList.indexOfFirst { it.summary.id == shortlistToggledPropertyId }
            val newListIdx = newDataList.indexOfFirst { it.summary.id == shortlistToggledPropertyId }
            shortlistToggledPropertyId = null // Make it null
            if (oldListIdx != -1 && newListIdx != -1) {
                dataList[oldListIdx] = newDataList[newListIdx]
                notifyItemChanged(oldListIdx, SHORTLIST)
                return
            }
        }

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

    companion object {
        private const val SHORTLIST = "shortlist"
    }
}

class PropertiesDiffCallBack(
    private val oldList: List<PropertySummaryUI>,
    private val newList: List<PropertySummaryUI>,
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].summary.id == newList[newItemPosition].summary.id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].summary == newList[newItemPosition].summary
    }
}