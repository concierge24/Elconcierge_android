package com.trava.user.ui.menu.emergencyContacts.contacts

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.trava.user.R
import com.trava.user.webservices.models.contacts.ContactModel
import com.trava.utilities.hideKeyboard
import kotlinx.android.synthetic.main.item_contact.view.*


/**
 * Created by ubuntu-android on 3/9/18.
 */
class ContactListAdapter(private val contactInterface: ContactInterface) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var TAG = javaClass.simpleName
    private var listContact = ArrayList<ContactModel>()
    private var filterList = ArrayList<ContactModel>()

    fun refreshList(listContact: ArrayList<ContactModel>) {
//        this.listContact.clear()
        this.filterList.clear()
        this.listContact = listContact
        this.filterList.addAll(this.listContact).let {
            notifyDataSetChanged()
        }
    }

    init {
//        this.filterList.addAll(this.listContact)
        Log.e("filter list size", "" + filterList.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CountryListItemView(LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false))
    }

    override fun getItemCount(): Int {
        return filterList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CountryListItemView) {
            holder.bind(filterList[position])
        }
    }

    inner class CountryListItemView(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init {
            itemView.rlMain.setOnClickListener(this)
        }

        fun bind(itemContact: ContactModel) = with(itemView) {
            tvContactNo.text = itemContact.phoneNumber
            if(itemContact.name.isNullOrEmpty()){
                tvName.text = itemContact.uniqueId
            }else{
                if (itemContact.uniqueId.isNullOrEmpty()){
                    tvName.text = itemContact.name
                }else{
                    tvName.text = String.format("%s","${itemContact.name}")
                }
            }
            if (!itemContact.name.isNullOrEmpty())
                tvChar.text = itemContact.name?.substring(0, 1)?.toUpperCase()
            if (itemContact.uniqueId!=null) {
                tvChar.visibility = View.GONE
                Glide.with(itemView.context).load(R.drawable.ic_placeholder).into(ivImage)
                ivImage.borderColor = ContextCompat.getColor(itemView.context,R.color.colorPrimary)

            }else{
                when(adapterPosition % 6){
                    0 -> Glide.with(itemView.context).load(ContextCompat.getDrawable(itemView.context,R.color.chat)).into(ivImage)
                    1 -> Glide.with(itemView.context).load(ContextCompat.getDrawable(itemView.context,R.color.notification)).into(ivImage)
                    2 -> Glide.with(itemView.context).load(ContextCompat.getDrawable(itemView.context,R.color.home)).into(ivImage)
                    3 -> Glide.with(itemView.context).load(ContextCompat.getDrawable(itemView.context,R.color.wallet)).into(ivImage)
                    4 -> Glide.with(itemView.context).load(ContextCompat.getDrawable(itemView.context,R.color.payment)).into(ivImage)
                    5 -> Glide.with(itemView.context).load(ContextCompat.getDrawable(itemView.context,R.color.friends)).into(ivImage)


                }

                tvChar.visibility = View.VISIBLE
                ivImage.borderColor = ContextCompat.getColor(itemView.context,R.color.white)
            }
        }


        override fun onClick(v: View?) {
            when (v?.id) {
                R.id.rlMain -> {
                    v.hideKeyboard()
                    contactInterface.getSelectedContactData(filterList[adapterPosition])
                }
            }
        }
    }

    // Filter Logic
    fun filter(text: String) {
        // || searchText.equals("+") || searchText.equals(String.format("%s","${"+"}${item?.phonecode}"))

        var searchText = text
        filterList.clear()
        if (searchText.isEmpty()) {
            filterList.addAll(listContact)
        } else {
            searchText = searchText.toLowerCase()
            for (item in listContact) {
                if (item.name?.toLowerCase()?.contains(searchText) == true || item.phoneNumber?.contains(searchText) == true) {
                    filterList.add(item)
                }
            }
//            contactInterface.getFilterListSize(filterList.size)
        }
        notifyDataSetChanged()
    }
}