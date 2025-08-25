package com.example.houserentalapp.presentation.ui.property

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.houserentalapp.R
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.presentation.utils.extensions.dpToPx
import com.example.houserentalapp.presentation.utils.extensions.logError
import com.example.houserentalapp.presentation.utils.extensions.logInfo
import com.example.houserentalapp.presentation.utils.extensions.logWarning
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.ShapeAppearanceModel
import java.io.File


class PropertiesAdapter(val onClick: (Long) -> Unit) : RecyclerView.Adapter<PropertiesAdapter.ViewHolder>()
//    : ListAdapter<PropertySummary, PropertiesAdapter.ViewHolder>(DiffCallBack())
{
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private var imageContainer: LinearLayout = itemView.findViewById(R.id.imageContainer)
        private var tvHeader: TextView = itemView.findViewById(R.id.tvHeader)
        private var tvBody1: TextView = itemView.findViewById(R.id.tvBody1)
        private var tvBody2: TextView = itemView.findViewById(R.id.tvBody2)
        private var tvFooter: TextView = itemView.findViewById(R.id.tvFooter)

        fun bind(summary: PropertySummary) {
            val screenWidth = itemView.context.resources.displayMetrics.widthPixels
            val imageWidth = (screenWidth / 2.2).toInt()

            // Add images programmatically
            if (summary.images.isNotEmpty())
                summary.images.forEach{
                    try {
                        // Get Image File
                        val file = File(itemView.context.filesDir, it.imageAddress)
                        if (!file.exists()) {
                            logWarning(
                                "Image(${it.imageAddress}) is not exists, property:${summary.id}"
                            )
                            return
                        }

                        // Add Image Into imageContainer
                        val shapableImageView = ShapeableImageView(itemView.context).apply {
                            scaleType = ImageView.ScaleType.CENTER_CROP
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
//                        Glide.with(itemView) // TODO: Need to check this
//                            .load(file)
//                            .into(shapableImageView)
                        shapableImageView.setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))
                        imageContainer.addView(shapableImageView)
                    } catch (exp: Exception) {
                        logError("Error on Add images programmatically exp:${exp.message}")
                    }
                }
            else // Add Place Holder Image
                repeat(2) {
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

            // Set On Click
            imageContainer.setOnClickListener {
                logInfo("Property ${summary.id} Clicked")
                onClick(summary.id)
            }
            itemView.setOnClickListener {
                logInfo("Property ${summary.id} Clicked")
                onClick(summary.id)
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
        val diffCallBack = PropertiesDiffCallBack(dataList, newDataList)
        val diffResult = DiffUtil.calculateDiff(diffCallBack)

        dataList.clear()
        dataList.addAll(newDataList)
        diffResult.dispatchUpdatesTo(this)
    }


    fun appendDataList(newDataList: List<PropertySummary>) {
        val startPosition = newDataList.size
        dataList.addAll(newDataList)
        notifyItemRangeInserted(startPosition ,newDataList.size)
    }
}

class PropertiesDiffCallBack(
    private val oldList: List<PropertySummary>,
    private val newList: List<PropertySummary>,
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}