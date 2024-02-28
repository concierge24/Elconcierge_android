package com.trava.user.ui.menu.emergencyContacts

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.trava.user.R
import com.trava.user.databinding.ItemEContactBinding
import com.trava.user.databinding.SosContactsLayoutBinding
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.models.eContacts.EContactsListing
import kotlinx.android.synthetic.main.sos_contacts_layout.view.*

class SosContactsAdapter(var mContext: Context?, var listRequests: ArrayList<EContactsListing>, var onDeleteButton: OnDeleteButton) : RecyclerView.Adapter<MyHolder>(){

    //    OnDeleteButton onDeleteButton;
    open interface OnDeleteButton {
        fun  OnContactDelete(pos : Int)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        var sosContactBind: SosContactsLayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.sos_contacts_layout, parent, false)
        sosContactBind.color = ConfigPOJO.Companion
        
        return MyHolder(sosContactBind.root)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        if (listRequests.get(position).phone_number=="0")
        {
            holder.contactNo.setText(listRequests.get(position).name)
            holder.tvContactName.text = ""
        }
        else
        {
            holder.contactNo.setText(listRequests.get(position).phone_number)
            holder.tvContactName.setText(listRequests.get(position).name)
        }

        holder.rl_root.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
        holder.ivCallButton.setColorFilter(Color.parseColor(ConfigPOJO.primary_color), PorterDuff.Mode.SRC_IN)
        holder.ivDeleteButton.setColorFilter(Color.parseColor(ConfigPOJO.primary_color), PorterDuff.Mode.SRC_IN)

        holder.ivCallButton.setOnClickListener {
                val phone = listRequests?.get(position).phone_number.toString()
                val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
                it.context?.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
        }

        holder.ivDeleteButton.setOnClickListener {
            onDeleteButton.OnContactDelete(position)
        }
    }

    override fun getItemCount(): Int {
        return listRequests.size;
    }
}

open class MyHolder (view : View) : RecyclerView.ViewHolder(view){
    var contactNo  = view.tvContactNumber
    var tvContactName  = view.tvContactName
    var ivCallButton  = view.ivCallButton
    var ivDeleteButton  = view.ivDeleteButton
    var rl_root  = view.rl_root
}