package com.trava.user.ui.home.dropofflocation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.trava.user.R
import com.trava.user.webservices.models.FindLocationPOJO
import kotlinx.android.synthetic.main.layout_location_view.view.*
import java.util.*
import kotlin.collections.ArrayList

class FindLocationsAdapter(var list: ArrayList<FindLocationPOJO>, var listener: ItemClickList, var lat: Double, var lng: Double) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FindLocationsAdapter.RecentLocViewHolder =
            RecentLocViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_location_view, parent,
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
            itemView.place_item_view.setOnClickListener {
                listener.itemSelect(list[adapterPosition], adapterPosition)
            }
        }

        fun onBind(data: FindLocationPOJO) = with(itemView) {
            place_area.text = data.address
            place_address.text = data.vicinity
            place_distance.text = getFormattedDecimal(distance(lat, lng, data.lat.toDouble(), data.lng.toDouble())) + " Km"
        }

        private fun getFormattedDecimal(num: Double): String? {
            return String.format(Locale.US, "%.2f", num)
        }

        private fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val theta = lon1 - lon2
            var dist = ((Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))) + (Math.cos(deg2rad(lat1))
                    * Math.cos(deg2rad(lat2))
                    * Math.cos(deg2rad(theta))))
            dist = Math.acos(dist)
            dist = rad2deg(dist)
            dist *= 60.0 * 1.1515
            return (dist)
        }

        private fun deg2rad(deg: Double): Double {
            return (deg * Math.PI / 180.0)
        }

        private fun rad2deg(rad: Double): Double {
            return (rad * 180.0 / Math.PI)
        }
    }
}

interface ItemClickList {
    fun itemSelect(key: FindLocationPOJO, position: Int)
}