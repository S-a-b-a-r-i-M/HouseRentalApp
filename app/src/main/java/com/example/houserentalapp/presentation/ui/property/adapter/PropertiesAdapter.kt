package com.example.houserentalapp.presentation.ui.property.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R
import com.example.houserentalapp.presentation.model.PropertySummaryUI
import com.example.houserentalapp.presentation.ui.base.BaseDiffCallBack
import com.example.houserentalapp.presentation.ui.base.BaseLoadingAdapter
import com.example.houserentalapp.presentation.ui.base.LoadingAdapterData
import com.example.houserentalapp.presentation.utils.extensions.getShapableImageView
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.helpers.loadImageSourceToImageView

typealias AdapterSummaryData = LoadingAdapterData.Data<PropertySummaryUI>

class PropertiesAdapter(val onClick: (Long) -> Unit, val onShortlistToggle: ((Long) -> Unit)? = null)
    : BaseLoadingAdapter<PropertySummaryUI>() {
    inner class DataViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
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
                        loadImageSourceToImageView(it.imageSource, shapableImageView)
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
                shapableImageView.setImageResource(R.drawable.no_image)
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

    class LoaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private var summaryUIList = listOf<PropertySummaryUI>() // Raw Data content
    private var shortlistToggledPropertyId: Long? = null

    override fun createDiffCallBack(
        oldList: List<LoadingAdapterData<PropertySummaryUI>>,
        newList: List<LoadingAdapterData<PropertySummaryUI>>
    ): BaseDiffCallBack<PropertySummaryUI> {
        return PropertiesDiffCallBack(oldList, newList)
    }

    override fun onCreateDataViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.single_property_summary_layout,
            parent,
            false
        )
        return DataViewHolder(view)
    }

    override fun onCreateLoaderViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.loader_item, parent, false
        )
        return LoaderViewHolder(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        logInfo("<------- onCreateViewHolder --------> ")
        return when(viewType) {
            LOADER_VIEW_TYPE -> onCreateLoaderViewHolder(parent)
            DATA_VIEW_TYPE -> onCreateDataViewHolder(parent)
            else -> throw IllegalArgumentException("Invalid View Type($viewType) given")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(val data = itemList[position]){
            is LoadingAdapterData.Data -> (holder as DataViewHolder).bind(data.data)
            LoadingAdapterData.Loader -> {}
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<Any?>) {
        when(holder){
            is DataViewHolder -> {
                if (payloads.isNotEmpty() && SHORTLIST == payloads[0]) {
                    val data = (itemList[position] as AdapterSummaryData).data
                    holder.bindShortlistData(data.isShortListed)
                    return
                }
            }
        }

        onBindViewHolder(holder, position) // FallBack
    }


    override fun setDataList(newDataList: List<PropertySummaryUI>, hasMore: Boolean) {
        if (shortlistToggledPropertyId != null && newDataList.size == summaryUIList.size) {
            val oldListIdx = itemList.indexOfFirst {
                it is AdapterSummaryData &&
                it.data.summary.id == shortlistToggledPropertyId
            }
            val newListIdx = newDataList.indexOfFirst { it.summary.id == shortlistToggledPropertyId }
            shortlistToggledPropertyId = null // Make it null
            if (oldListIdx != -1 && newListIdx != -1) {
                itemList[oldListIdx] = LoadingAdapterData.Data(newDataList[newListIdx])
                notifyItemChanged(oldListIdx, SHORTLIST)
                return
            }
        }

        summaryUIList = newDataList // Store raw data content
        super.setDataList(newDataList, hasMore)
    }

    companion object {
        private const val SHORTLIST = "shortlist"
    }
}

class PropertiesDiffCallBack(
    oldList: List<LoadingAdapterData<PropertySummaryUI>>,
    newList: List<LoadingAdapterData<PropertySummaryUI>>,
) : BaseDiffCallBack<PropertySummaryUI>(oldList, newList) {
    override fun areDataItemsSame(
        oldData: PropertySummaryUI,
        newData: PropertySummaryUI
    ): Boolean = oldData.summary.id == newData.summary.id
}