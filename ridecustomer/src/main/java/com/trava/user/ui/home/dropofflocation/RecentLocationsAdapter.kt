package com.trava.user.ui.home.dropofflocation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.trava.user.R
import com.trava.user.databinding.ItemRecentLocationsBinding
import com.trava.user.utils.ConfigPOJO
import com.trava.user.webservices.models.RecentItem
import kotlinx.android.synthetic.main.item_recent_locations.view.*

class RecentLocationsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var list =  ArrayList<RecentItem>()
    private var listener : RecentLocationinterface? = null
    fun refreshList(list: ArrayList<RecentItem>,listener : RecentLocationinterface){
        this.list = list
        this.listener = listener
        notifyDataSetChanged()

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var otpBinding:ItemRecentLocationsBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),R.layout.item_recent_locations, parent, false)
        otpBinding.color = ConfigPOJO.Companion
        return RecentLocViewHolder(otpBinding.root)
    }

    override fun getItemCount(): Int {
        return list.size

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is RecentLocViewHolder){
            holder.onBind(list[position])
        }
    }

    inner class RecentLocViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        init {
            itemView.rlRecentLocation.setOnClickListener {
                listener?.getSelectedRecentLocation(list[adapterPosition])
            }
        }

        fun onBind(data : RecentItem) = with(itemView){
            tvLocTitle.text = data.addressName
            tvLocAddress.text = data.address

        }
    }
}

interface RecentLocationinterface{
    fun getSelectedRecentLocation(data : RecentItem)
}