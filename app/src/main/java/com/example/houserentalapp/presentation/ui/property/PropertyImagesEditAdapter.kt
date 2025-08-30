package com.example.houserentalapp.presentation.ui.property

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R

class PropertyImagesEditAdapter(val onDeleteBtnClick: (Uri) -> Unit) : RecyclerView.Adapter<PropertyImagesEditAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val ibtnDelete: ImageButton = itemView.findViewById(R.id.ibtnDeleteImage)

        fun bind(imageUri: Uri) {
            imageView.setImageURI(imageUri)
            ibtnDelete.setOnClickListener {
                onDeleteBtnClick(imageUri)
            }
        }
    }

    private val imagesUri = mutableListOf<Uri>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.single_image_edit_view, parent, false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(imagesUri[position])
    }

    override fun getItemCount() = imagesUri.size

    fun setData(newUris: List<Uri>) {
        imagesUri.clear()
        imagesUri.addAll(newUris)
        notifyDataSetChanged()
        // TODO: Use DIff Util
    }
}