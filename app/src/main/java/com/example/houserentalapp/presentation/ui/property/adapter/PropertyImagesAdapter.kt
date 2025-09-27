package com.example.houserentalapp.presentation.ui.property.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R
import com.example.houserentalapp.domain.model.PropertyImage
import com.example.houserentalapp.presentation.utils.helpers.loadImageSourceToImageViewV2

class PropertyImagesViewAdapter(private val onClick: (List<PropertyImage>) -> Unit)
    : RecyclerView.Adapter<PropertyImagesViewAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewProperty)

        fun bind(propertyImage: PropertyImage) {
            loadImageSourceToImageViewV2(propertyImage.imageSource, imageView)
            imageView.setOnClickListener { onClick(propertyImages) }
        }
    }

    private val propertyImages = mutableListOf<PropertyImage>()

    fun setPropertyImages(images: List<PropertyImage>) {
        propertyImages.addAll(images)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_imageview_layout, parent, false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(propertyImages[position])
    }

    override fun getItemCount() = propertyImages.size
}