package com.example.houserentalapp.presentation.ui.common.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R
import com.example.houserentalapp.domain.model.PropertyFilters
import com.example.houserentalapp.presentation.utils.extensions.logDebug

class SearchHistoryAdapter(private val onItemClick: (PropertyFilters) -> Unit) : RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSearchQuery: TextView = itemView.findViewById(R.id.tvSearchQuery)

        fun bind(data: PropertyFilters) {
            tvSearchQuery.text = data.searchQuery
            itemView.setOnClickListener {
               onItemClick(data)
            }
        }
    }

    private var dataList: List<PropertyFilters> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_search_history, parent, false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int = dataList.count()

    fun setDateList(newData: List<PropertyFilters>) {
        dataList = newData
        notifyDataSetChanged()
        logDebug("new search history applied")
    }
}