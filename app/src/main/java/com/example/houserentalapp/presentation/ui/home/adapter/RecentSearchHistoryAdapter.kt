package com.example.houserentalapp.presentation.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R
import com.example.houserentalapp.domain.model.PropertyFilters
import com.example.houserentalapp.presentation.utils.extensions.logDebug

sealed class AdapterDataWithFooter {
    data class Data<T>(val data: T) : AdapterDataWithFooter()
    object Footer : AdapterDataWithFooter()
}

class RecentSearchHistoryAdapter(
    private val onItemClick: (PropertyFilters) -> Unit
) : RecyclerView.Adapter<RecentSearchHistoryAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSearchSummary: TextView = itemView.findViewById(R.id.tvSearchSummary)
        val ibtnForward: ImageButton = itemView.findViewById(R.id.ibtnForward)

        fun bind(data: PropertyFilters) {
            tvSearchSummary.text = getFiltersSummary(data)
            tvSearchSummary.setOnClickListener { onItemClick(data) }
            itemView.setOnClickListener { onItemClick(data) }
            ibtnForward.setOnClickListener { onItemClick(data) }
        }

        private fun getFiltersSummary(data: PropertyFilters): String {
            val parts = mutableListOf<String>()
            with(data) {
                if (searchQuery.isNotEmpty())
                    parts.add(searchQuery.replaceFirstChar { it.uppercase() })

                // Add BHK types
                parts.add(
                    when {
                        bhkTypes.isEmpty() -> "Any BHK"
                        bhkTypes.size == 1 -> bhkTypes[0].readable
                        else -> bhkTypes.joinToString(",") {
                            it.readable.substring(
                                0,
                                1
                            )
                        } + " BHK"
                    })

                // Add property types
                parts.add(
                    when {
                    propertyTypes.isEmpty() -> "Any Property Type"
                    propertyTypes.size == 1 -> propertyTypes[0].readable
                    else -> propertyTypes.joinToString(", ") { it.readable }
                })

                // Add budget
                budget?.let { (min, max) ->
                    parts.add("₹${min.formatCurrency()} - ₹${max.formatCurrency()}")
                } ?: parts.add("Any Budget")

                // Add furnishing types
                parts.add(
                    when {
                        furnishingTypes.isEmpty() -> "Any Furnishing"
                        furnishingTypes.size == 1 -> furnishingTypes[0].readable
                        else -> "Multiple Furnishing Types"
                    }
                )

                // Add tenant types
                parts.add(when {
                    tenantTypes.isEmpty() -> "Any Tenant"
                    tenantTypes.size == 1 -> tenantTypes[0].readable
                    else -> tenantTypes.joinToString(", ") { it.readable }
                })
            }

            return parts.joinToString(", ")
        }

        // Helper extension for currency formatting
        private fun Float.formatCurrency(): String {
            return when {
                this >= 1_00_000 ->  "%.2f L".format(this / 1_00_000.0)
                this >= 1000 -> "%.1f K".format(this / 1000.0)
                else -> "$this"
            }
        }
    }

    private var dataList: List<PropertyFilters> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.single_recent_history_card, parent, false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int = dataList.size

    fun setDateList(newData: List<PropertyFilters>) {
        dataList = newData
        notifyDataSetChanged()
        logDebug("new search history applied")
    }
}