package com.trava.user.ui.home.dropofflocation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.trava.user.R
import com.trava.user.webservices.models.LocationPOJO
import kotlinx.android.synthetic.main.item_string.view.*

class NearLocationsAdapter(var list: ArrayList<LocationPOJO>, var listener: LocationInterface) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NearLocationsAdapter.RecentLocViewHolder =
            RecentLocViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_string, parent,
                    false))

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RecentLocViewHolder) {
            holder.onBind(list[position])
        }
    }

    inner class RecentLocViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.tvContactName.setOnClickListener {
                listener.getSelectedLocation(list[adapterPosition].keys, adapterPosition)
            }
        }

        fun onBind(data: LocationPOJO) = with(itemView) {
            tvContactName.text = data.name
            if (data.temp) {
                tvContactName.setBackgroundResource(R.drawable.status_change)
            } else {
                tvContactName.setBackgroundResource(R.drawable.status_change_line)
            }
        }
    }
}

interface LocationInterface {
    fun getSelectedLocation(key: String, position: Int)
}