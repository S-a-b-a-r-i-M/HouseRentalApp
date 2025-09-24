package com.example.houserentalapp.presentation.ui.property.adapter

import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R
import com.example.houserentalapp.domain.model.ImageSource
import com.example.houserentalapp.domain.model.PropertyImage
import java.io.File

class PropertyImagesViewAdapter(private val onClick: (List<PropertyImage>) -> Unit)
    : RecyclerView.Adapter<PropertyImagesViewAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewProperty)

        fun bind(filePath: String) {
            val file = File(filePath)
            if (file.exists()){
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                imageView.setImageBitmap(bitmap)
                setOnClick()
            }
        }

        fun bind(uri: Uri) {
            imageView.setImageURI(uri)
            setOnClick()
        }

        fun setOnClick() {
            imageView.setOnClickListener { onClick(propertyImages) }
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
        when(val imageSource = propertyImages[position].imageSource) {
            is ImageSource.LocalFile -> {
                holder.bind(imageSource.filePath)
            }
            is ImageSource.Uri -> {
                holder.bind(imageSource.uri)
            }
        }
    }

    override fun getItemCount() = propertyImages.size
}