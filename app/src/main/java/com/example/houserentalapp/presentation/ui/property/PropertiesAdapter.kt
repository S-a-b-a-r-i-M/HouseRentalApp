package com.example.houserentalapp.presentation.ui.property

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.presentation.utils.extensions.dpToPx
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.ShapeAppearanceModel

class PropertiesAdapter() : RecyclerView.Adapter<PropertiesAdapter.ViewHolder>() {
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private var imageContainer: LinearLayout = itemView.findViewById(R.id.imageContainer)
        private var tvHeader: TextView = itemView.findViewById(R.id.tvHeader)
        private var tvBody1: TextView = itemView.findViewById(R.id.tvBody1)
        private var tvBody2: TextView = itemView.findViewById(R.id.tvBody2)
        private var tvFooter: TextView = itemView.findViewById(R.id.tvFooter)

        fun bind(summary: PropertySummary) {
            logInfo("itemView context: ${itemView.context}")
            val screenWidth = itemView.context.resources.displayMetrics.widthPixels
            val imageWidth = (screenWidth / 2.2).toInt()

            // Add images programmatically
            repeat(4) {
                val shapableImageView = ShapeableImageView(itemView.context).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageResource(
                        listOf(R.drawable.interior, R.drawable.room_1, R.drawable.room_2, R.drawable.room_3).random()
                    )
                    shapeAppearanceModel = ShapeAppearanceModel.builder().apply {
                        setAllCornerSizes(24f)
                    }.build()


                    val marginInPx = 5.dpToPx(itemView.context)
                    setLayoutParams(
                        LinearLayout.LayoutParams(
                            imageWidth,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        ).apply {
                            setMargins(marginInPx, 0, marginInPx, 0)
                        }
                    )
                }
                imageContainer.addView(shapableImageView)
            }

            with(summary) {
                tvHeader.text = "${summary.name} (${summary.bhk.readable})"
                tvBody1.text = "${summary.address.city}, ${summary.address.locality}"
                tvBody2.text = "${summary.price}"
                tvFooter.text = "${summary.bhk.readable} | ${summary.furnishingType.readable} | ${summary.builtUpArea} sq.ft."
            }

            // On Item Click
            itemView.setOnClickListener {
                logInfo("Property ${summary.id} Clicked")
            }
        }
    }
    private var dataList: MutableList<PropertySummary> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_property_summary_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount() = dataList.size

    fun setDataList(newDataList: List<PropertySummary>) {
        // Should i replace completely or add only new data ??
        dataList = newDataList.toMutableList()
        notifyDataSetChanged()
    }
}