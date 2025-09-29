package com.example.houserentalapp.presentation.ui.listings.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.houserentalapp.R
import com.example.houserentalapp.domain.model.Lead
import com.example.houserentalapp.domain.model.PropertySummary
import com.example.houserentalapp.presentation.utils.extensions.openDialer
import com.example.houserentalapp.presentation.utils.extensions.openMail

class LeadsAdapter (private val onClick: (Lead) -> Unit)
    : RecyclerView.Adapter<LeadsAdapter.ViewHolder>()
{
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvLeadName = itemView.findViewById<TextView>(R.id.tvLeadName)
        private val ibtnOpenLead = itemView.findViewById<ImageButton>(R.id.ibtnOpenLead)
        private val tvInterestedProperties = itemView.findViewById<TextView>(R.id.tvInterestedProperties)
        private val btnDialLead = itemView.findViewById<ImageButton>(R.id.ibtnDial)
        private val btnEmailLead = itemView.findViewById<ImageButton>(R.id.ibtnEmail)


        fun bind(lead: Lead) {
            tvLeadName.text = lead.leadUser.name
            tvInterestedProperties.text = getPropertiesSummaryText(
                lead.interestedPropertiesWithStatus.map { it.first }
            )
            val context = itemView.context
            btnDialLead.setOnClickListener { context.openDialer(lead.leadUser.phone) }
            if (lead.leadUser.email != null) {
                btnEmailLead.visibility = View.VISIBLE
                btnEmailLead.setOnClickListener { context.openMail(lead.leadUser.email) }
            }
            else
                btnEmailLead.visibility = View.GONE

            // OnClick Listener
            itemView.setOnClickListener { onClick(lead) }
            ibtnOpenLead.setOnClickListener { onClick(lead) }
            tvInterestedProperties.setOnClickListener { onClick(lead) }
        }

        private fun getPropertiesSummaryText(summaryList: List<PropertySummary>, max: Int = 2): String {
            var str = ""
            for (i in 0 until summaryList.size.coerceAtMost(max)) {
                val it = summaryList[i]
                str += "‣ ${it.bhk} • ${it.address.city}, ${it.address.locality} • ₹${it.price}/month \n"
            }
            if(summaryList.size > max) str += "${summaryList.size - max} More . . ."
            return str.substring(0, str.length-1)
        }
    }

    private val leads : MutableList<Lead> = mutableListOf()

    fun setLeads(newLeads: List<Lead>) {
        val diffCallBack = LeadsDiffCallBack(leads, newLeads)
        val diffResult = DiffUtil.calculateDiff(diffCallBack)

        leads.clear()
        leads.addAll(newLeads)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_lead_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(leads[position])
    }

    override fun getItemCount() = leads.size
}

class LeadsDiffCallBack(
    private val oldList: List<Lead>,
    private val newList: List<Lead>,
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}