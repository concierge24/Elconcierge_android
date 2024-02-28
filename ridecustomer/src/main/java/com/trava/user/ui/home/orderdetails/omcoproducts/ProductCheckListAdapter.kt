package com.trava.user.ui.home.orderdetails.omcoproducts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.trava.user.R
import com.trava.user.databinding.ProductchecklistItemlayoutBinding
import com.trava.user.utils.ConfigPOJO
import com.trava.user.webservices.models.ProductCheckListModel
import kotlinx.android.synthetic.main.productchecklist_itemlayout.view.*

class ProductCheckListAdapter(var mContext: Context?, val productModeList: ArrayList<ProductCheckListModel>, val listClickCallback: CheckListClickCallback)
    : RecyclerView.Adapter<ProductChecklistMyHolder>() {
    var state: String? = null

    interface CheckListClickCallback {
        fun checkListClickCallback(vi: View, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductChecklistMyHolder {
        var binding: ProductchecklistItemlayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.productchecklist_itemlayout, parent, false)
        binding.color = ConfigPOJO.Companion
        return ProductChecklistMyHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ProductChecklistMyHolder, position: Int) {
        holder.name.setText(productModeList.get(position).product_code)
        holder.phone.setText(productModeList.get(position).phone_number.toString())
        holder.tvProductNumber.setText(mContext?.getString(R.string.product)+""+(position+1))


        holder.iv_delete.setOnClickListener {
            listClickCallback.checkListClickCallback(it, position)
        }
    }

    override fun getItemCount(): Int {
        return productModeList.size;
    }

    public fun getList(): ArrayList<ProductCheckListModel> {
        return productModeList
    }
}

open class ProductChecklistMyHolder(view: View) : RecyclerView.ViewHolder(view) {
    var name = view.tvproductCode
    var phone = view.tvNumber
    var tvProductNumber = view.tvProductNumber
    var iv_delete = view.iv_delete
}