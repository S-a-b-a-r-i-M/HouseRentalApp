package com.example.houserentalapp.presentation.ui.property.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R

class PropertyImagesEditAdapter(val onDeleteBtnClick: (Uri) -> Unit) :
    RecyclerView.Adapter<PropertyImagesEditAdapter.ViewHolder>()
{
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val ibtnDelete: ImageButton = itemView.findViewById(R.id.ibtnDeleteImage)

        fun bind(imageUri: Uri) {
            imageView.setImageURI(imageUri)
            ibtnDelete.setOnClickListener {
                deleteActionTriggeredUri = imageUri
                onDeleteBtnClick(imageUri)
            }
        }
    }

    private val imagesUri = mutableListOf<Uri>()
    private var deleteActionTriggeredUri: Uri? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.single_image_edit_view, parent, false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(imagesUri[position])
    }

    override fun getItemCount() = imagesUri.size

    fun setDataList(newUris: List<Uri>) {
        // Check if this is a result of deletion
        // TODO: IS this worth of handling separately ?
        if (deleteActionTriggeredUri != null && imagesUri.size - 1 == newUris.size ) {
            val removedItemIdx = imagesUri.indexOfFirst { it == deleteActionTriggeredUri }
            deleteActionTriggeredUri = null // make it null
            imagesUri.removeAt(removedItemIdx)
            notifyItemRemoved(removedItemIdx)
            return
        }

        // Calculate the differences using diff util
        val diffCallBack = PropertyImagesDiffCallBack(imagesUri, newUris)
        val diffResult = DiffUtil.calculateDiff(diffCallBack)
        imagesUri.clear()
        imagesUri.addAll(newUris)
        diffResult.dispatchUpdatesTo(this)
    }
}

class PropertyImagesDiffCallBack(
    private val oldList: List<Uri>,
    private val newList: List<Uri>
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