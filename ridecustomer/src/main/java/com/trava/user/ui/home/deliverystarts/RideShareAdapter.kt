package com.trava.user.ui.home.deliverystarts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.trava.user.R
import com.trava.user.webservices.models.contacts.ContactModel
import kotlinx.android.synthetic.main.item_ride_share.view.*

class RideShareAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var list =  ArrayList<ContactModel>()

    fun setList(list : ArrayList<ContactModel>){
        this.list = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return RideShareViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_ride_share,parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is RideShareViewHolder){
            holder.onBind(list[position])
        }
    }

    inner class RideShareViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(data : ContactModel) = with(itemView){
            tvContactName.text = data.name
        }
    }
}