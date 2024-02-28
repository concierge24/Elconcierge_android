package com.trava.user.ui.menu.emergencyContacts.contacts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.trava.user.R
import com.trava.user.webservices.models.contacts.ContactModel
import kotlinx.android.synthetic.main.item_contacts.view.*

class ContactsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var list: ArrayList<ContactModel> = ArrayList()
    private var listener : ContactsInterface? = null

    fun setList(list: ArrayList<ContactModel>, listener : ContactsInterface) {
        this.list = list
        this.listener = listener
        notifyDataSetChanged()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ContactsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_contacts, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ContactsViewHolder){
            holder.onBind(list[position])
        }
    }

    inner class ContactsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.ivCancel.setOnClickListener {
                listener?.deleteContact(adapterPosition)
            }
        }

        fun onBind(data: ContactModel) = with(itemView) {
            tvContactName.text = data.name
            tvContactNumber.text = data.phoneNumber
        }
    }
}

interface ContactsInterface {
    fun deleteContact(position: Int)
}