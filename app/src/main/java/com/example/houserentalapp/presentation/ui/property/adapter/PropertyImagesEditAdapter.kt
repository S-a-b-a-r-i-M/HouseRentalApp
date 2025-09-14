package com.example.houserentalapp.presentation.ui.property.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R
import com.example.houserentalapp.domain.model.ImageSource
import com.example.houserentalapp.domain.model.PropertyImage
import com.example.houserentalapp.presentation.utils.extensions.logWarning
import com.example.houserentalapp.presentation.utils.helpers.loadImageSourceToImageView
import com.google.android.material.imageview.ShapeableImageView
import java.io.File

class PropertyImagesEditAdapter(val onDeleteBtnClick: (PropertyImage) -> Unit) :
    RecyclerView.Adapter<PropertyImagesEditAdapter.ViewHolder>()
{
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imageView: ShapeableImageView = itemView.findViewById(R.id.imageView)
        val ibtnDelete: ImageButton = itemView.findViewById(R.id.ibtnDeleteImage)

        fun bind(propertyImage: PropertyImage) {
            loadImageSourceToImageView(propertyImage.imageSource, imageView)
            ibtnDelete.setOnClickListener {
                deleteActionTriggeredUri = propertyImage
                onDeleteBtnClick(propertyImage)
            }
        }
    }

    private val images = mutableListOf<PropertyImage>()
    private var deleteActionTriggeredUri: PropertyImage? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.single_image_edit_view, parent, false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount() = images.size

    fun setDataList(newImages: List<PropertyImage>) {
        // Check if this is a result of deletion
        // TODO: IS this worth of handling separately ?
        if (deleteActionTriggeredUri != null && images.size - 1 == newImages.size ) {
            val removedItemIdx = images.indexOfFirst { it == deleteActionTriggeredUri }
            deleteActionTriggeredUri = null // make it null
            images.removeAt(removedItemIdx)
            notifyItemRemoved(removedItemIdx)
            return
        }

        // Calculate the differences using diff util
        val diffCallBack = PropertyImagesDiffCallBack(images, newImages)
        val diffResult = DiffUtil.calculateDiff(diffCallBack)
        images.clear()
        images.addAll(newImages)
        diffResult.dispatchUpdatesTo(this)
    }
}

class PropertyImagesDiffCallBack(
    private val oldList: List<PropertyImage>,
    private val newList: List<PropertyImage>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ) = oldList[oldItemPosition] == newList[newItemPosition]

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    )= oldList[oldItemPosition] == newList[newItemPosition]
}