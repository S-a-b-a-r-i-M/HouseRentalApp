package com.example.houserentalapp.presentation.ui.property.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R
import com.example.houserentalapp.domain.model.ImageSource
import com.example.houserentalapp.presentation.utils.helpers.loadImageSourceToImageViewV2

class ImagePagerAdapter(private val images: List<ImageSource>) :
    RecyclerView.Adapter<ImagePagerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image_view)

        fun bind(imageSource: ImageSource) {
            loadImageSourceToImageViewV2(imageSource, imageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_page, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount(): Int = images.size
}