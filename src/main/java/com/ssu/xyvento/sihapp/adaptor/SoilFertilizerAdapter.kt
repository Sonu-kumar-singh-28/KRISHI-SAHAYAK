package com.ssu.xyvento.sihapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssu.xyvento.sihapp.R
import com.ssu.xyvento.sihapp.dataclass.SoilAdvisory

class SoilFertilizerAdapter(
    private var list: List<SoilAdvisory>
) : RecyclerView.Adapter<SoilFertilizerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.tvAdvisory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_soil_fertilizer, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.text.text = list[position].message
    }

    fun updateData(newList: List<SoilAdvisory>) {
        list = newList
        notifyDataSetChanged()
    }
}
