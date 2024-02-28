package com.trava.user.ui.home.dropofflocation

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.trava.user.R
import com.trava.user.utils.ConfigPOJO
import com.trava.user.webservices.models.StopsModel
import kotlinx.android.synthetic.main.fragment_set_up_dropoff_location.*
import kotlinx.android.synthetic.main.ride_stops_layout.view.*

class StopsLocationsAdapter(var list: ArrayList<StopsModel>, var listener: StopsLocationinterface) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopsLocationsAdapter.RecentLocViewHolder =
            RecentLocViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.ride_stops_layout, parent,
                    false))

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RecentLocViewHolder) {
            holder.onBind(list[position],position)
        }
    }

    inner class RecentLocViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.tv_stop.setOnClickListener {
                listener?.getSelectedRecentLocation(list[adapterPosition], adapterPosition)
            }



            itemView.iv_remove.setOnClickListener {
                listener?.getSelectedRecentLocationDelete(list[adapterPosition], adapterPosition)
            }
        }

        fun onBind(data: StopsModel,position: Int) = with(itemView) {
            tv_stop.text = data.address

            when (position) {
                0 -> tv_main.setColorFilter(iv_remove.context.resources.getColor(R.color.transporter_base_color), PorterDuff.Mode.SRC_IN)
                1 -> tv_main.setColorFilter(iv_remove.context.resources.getColor(R.color.green_gradient_1), PorterDuff.Mode.SRC_IN)
                2 -> tv_main.setColorFilter(iv_remove.context.resources.getColor(R.color.payment), PorterDuff.Mode.SRC_IN)
                3 -> tv_main.setColorFilter(iv_remove.context.resources.getColor(R.color.mat_grey), PorterDuff.Mode.SRC_IN)
            }

            if (data.stop_status == "pending" || data.stop_status == "") {
                iv_remove.visibility = View.VISIBLE
            } else {
                iv_remove.visibility = View.GONE
            }
            if (data.stop_status == "pending") {
                iv_remove.setImageResource(R.drawable.ic_edit)
            } else {
                iv_remove.setImageResource(R.drawable.ic_action_cross)
            }
        }
    }
}

interface StopsLocationinterface {
    fun getSelectedRecentLocation(data: StopsModel, position: Int)
    fun getSelectedRecentLocationDelete(data: StopsModel, position: Int)
}