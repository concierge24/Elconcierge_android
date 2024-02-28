package com.trava.user.ui.home.dropofflocation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.trava.user.R
import com.trava.user.webservices.models.locationaddress.ResultItem
import kotlinx.android.synthetic.main.location_dropdown_item_layout.view.*

class LocationDropdownAdapter (context: Context, addressList: ArrayList<ResultItem>) : ArrayAdapter<ResultItem>(context, 0, addressList) {

    override fun getView(position: Int, recycledView: View?, parent: ViewGroup): View {
        return this.createView(position, recycledView, parent)
    }

    override fun getDropDownView(position: Int, recycledView: View?, parent: ViewGroup): View {
        return this.createView(position, recycledView, parent)
    }

    private fun createView(position: Int, recycledView: View?, parent: ViewGroup): View {
        val addressData = getItem(position)
        val view = recycledView ?: LayoutInflater.from(context).inflate(R.layout.location_dropdown_item_layout, parent, false)

        view.tvLocationAddress.text = addressData?.address+", "+addressData?.city+", "+addressData?.state+", "+addressData?.country
        return view
    }
}