package com.example.houserentalapp.presentation.ui.property

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R
import com.example.houserentalapp.domain.model.PropertyImage
import java.io.File

class PropertyImagesViewAdapter() : RecyclerView.Adapter<PropertyImagesViewAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageViewProperty)

        fun bind(fileName: String) {
            val file = File(itemView.context.filesDir, fileName)
            if (file.exists()){
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                imageView.setImageBitmap(bitmap)
            }
        }
    }

    private val propertyImages : MutableList<PropertyImage> = mutableListOf()

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
        holder.bind(propertyImages[position].imageAddress)
    }

    override fun getItemCount() = propertyImages.size
}