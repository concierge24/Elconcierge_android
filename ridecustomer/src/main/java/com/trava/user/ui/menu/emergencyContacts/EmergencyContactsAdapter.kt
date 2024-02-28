package com.trava.user.ui.menu.emergencyContacts

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
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.models.EContact
import kotlinx.android.synthetic.main.item_e_contact.view.*

class EmergencyContactsAdapter(private val response: List<EContact>?) : RecyclerView.Adapter<EmergencyContactsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var otpBinding:ItemEContactBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.item_e_contact, parent, false)
        otpBinding.color = ConfigPOJO.Companion
        return ViewHolder(otpBinding.root)
    }

    override fun getItemCount(): Int = response?.size ?: 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(response?.get(position))

    }

    inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView
            ?: View(itemView?.context)) {
        private var contactData: EContact? = null

        init {
            itemView?.setOnClickListener {
                val phone = contactData?.phoneNumber.toString()
                val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
                it.context?.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            }
        }

        fun bind(data: EContact?) {
            contactData = data
            itemView.tvContactName.text = data?.name
            itemView.tvContactNumber.text = data?.phoneNumber.toString()
            itemView.rr_view.background = StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)
            itemView.ivCallButton.setColorFilter(Color.parseColor(ConfigPOJO.primary_color), PorterDuff.Mode.SRC_IN)
        }
    }

}