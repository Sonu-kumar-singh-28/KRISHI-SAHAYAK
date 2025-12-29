package com.ssu.xyvento.sihapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssu.xyvento.sihapp.R
import com.ssu.xyvento.sihapp.dataclass.MarketPrice

class MarketPriceAdapter(
    private val fullList: List<MarketPrice>
) : RecyclerView.Adapter<MarketPriceAdapter.ViewHolder>() {

    private var filteredList = fullList.toMutableList()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val crop: TextView = view.findViewById(R.id.tvCropName)
        val mandi: TextView = view.findViewById(R.id.tvMandi)
        val min: TextView = view.findViewById(R.id.tvMinPrice)
        val max: TextView = view.findViewById(R.id.tvMaxPrice)
        val modal: TextView = view.findViewById(R.id.tvModalPrice)
        val date: TextView = view.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_market_price, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredList[position]
        holder.crop.text = item.commodity ?: "N/A"
        holder.mandi.text = "${item.market ?: "Unknown"} | ${item.state ?: "Unknown"}"
        holder.min.text = "Min ₹${item.min_price ?: "0"}"
        holder.max.text = "Max ₹${item.max_price ?: "0"}"
        holder.modal.text = "Modal ₹${item.modal_price ?: "0"}"
        holder.date.text = "Updated: ${item.arrival_date ?: "N/A"}"
    }

    override fun getItemCount(): Int = filteredList.size

    // 🔍 Search filter
    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            fullList.toMutableList()
        } else {
            fullList.filter {
                (it.commodity?.contains(query, true) ?: false) ||
                        (it.market?.contains(query, true) ?: false) ||
                        (it.state?.contains(query, true) ?: false)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }
    fun updateList(newList: List<MarketPrice>) {
        filteredList = newList.toMutableList()
        notifyDataSetChanged()
    }
}
