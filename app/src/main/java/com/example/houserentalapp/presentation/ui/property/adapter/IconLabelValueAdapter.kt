package com.example.houserentalapp.presentation.ui.property.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R

data class IconLabelValueData(val drawable: Drawable?, val label: String, val value: String)

class IconLabelValueAdapter : RecyclerView.Adapter<IconLabelValueAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val tvLabel: TextView = itemView.findViewById(R.id.tvLabel)
        val tvValue: TextView = itemView.findViewById(R.id.tvValue)

        fun bind(data: IconLabelValueData) {
            imageView.setImageDrawable(data.drawable)
            tvLabel.text = data.label
            tvValue.text = data.value
        }
    }

    private val dataList = mutableListOf<IconLabelValueData>()

    fun setDataList(newDataList: List<IconLabelValueData>) {
        dataList.clear()
        dataList.addAll(newDataList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_icon_value_label, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount() = dataList.size
}