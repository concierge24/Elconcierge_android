package com.trava.user.ui.home.orderdetails.omcoproducts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.trava.user.R
import com.trava.user.databinding.ProductlistItemlayoutBinding
import com.trava.user.utils.ConfigPOJO
import com.trava.user.webservices.models.ProductListModel
import kotlinx.android.synthetic.main.productlist_itemlayout.view.*


class ProductListAdapter (var mContext: Context?, val productModeList: ArrayList<ProductListModel>,  val  listClickCallback : ListClickCallback) : RecyclerView.Adapter<MyHolder>() {
    var state: String? = null

    interface ListClickCallback {
        fun listClickCallback(vi: View,position:Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        var binding: ProductlistItemlayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.productlist_itemlayout, parent, false)
        binding.color = ConfigPOJO.Companion
        return MyHolder(binding.root)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.name.setText(productModeList.get(position).product_name)
        holder.receiver.setText(productModeList.get(position).receiver_name.toString())
        holder.phonenumber.setText(productModeList.get(position).receiver_phone_number)
        holder.tvProductNumber.setText(mContext!!.getString(R.string.product)+""+(position+1))

        holder.iv_delete.setOnClickListener {
            listClickCallback.listClickCallback(it,position)
        }
     }

    override fun getItemCount(): Int {
        return productModeList.size;
    }

    public fun getList(): ArrayList<ProductListModel>
    {
        return productModeList
    }
}

open class MyHolder(view: View) : RecyclerView.ViewHolder(view) {
    var name = view.tvproductName
    var receiver = view.tvRxName
    var phonenumber = view.tvNumber
    var tvProductNumber = view.tvProductNumber
    var iv_delete = view.iv_delete
}