package com.trava.user.ui.home.orderbreakdown

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.trava.user.R
import com.trava.user.databinding.ChecklistItemlayoutBinding
import com.trava.user.ui.home.vehicles.SelectVehicleTypeAdapter
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.models.CheckListModel
import com.trava.user.webservices.models.order.CheckListModelArray
import kotlinx.android.synthetic.main.checklist_itemlayout.view.*

class CheckListDetailsAdapter(var mContext: Context?, val checkListModelList: ArrayList<CheckListModelArray>,
                       val  onInfoCallBack : OnInfocallBack) : RecyclerView.Adapter<MyHolder>() {

    var state: String? = null

    open interface OnInfocallBack
    {
        fun onInfoClickDelete(vi : View,position: Int)
        fun onInfoClickEdit(tModelList: ArrayList<CheckListModelArray>)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        var binding: ChecklistItemlayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.checklist_itemlayout, parent, false)
        binding.color = ConfigPOJO.Companion
        return MyHolder(binding.root)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.name.setText(checkListModelList.get(position).item_name)
        holder.price.setText(checkListModelList.get(position).after_item_price.toString())
        holder.tax.setText("*Tax price "+checkListModelList.get(position).tax.toString()+" "+ConfigPOJO.currency)
        holder.tax.visibility=View.VISIBLE
        holder.price.background= StaticFunction.changeBorderTextColor(ConfigPOJO.white_color, ConfigPOJO.primary_color, GradientDrawable.RECTANGLE)

        if (checkListModelList[position].updated_by=="2")
        {
            holder.price.setOnKeyListener(null)
            holder.price.isFocusable=false
            holder.price.isFocusableInTouchMode=false
            holder.iv_delete.visibility=View.GONE
        }

        holder.iv_delete.setOnClickListener {
            onInfoCallBack.onInfoClickDelete(it,position)
        }

        holder.price.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s:CharSequence, start:Int, count:Int, after:Int) {
            }
            override fun onTextChanged(s:CharSequence, start:Int, before:Int, count:Int) {
                checkListModelList.get(position).after_item_price=s.toString()
                onInfoCallBack.onInfoClickEdit(checkListModelList)
            }
            override fun afterTextChanged(s: Editable) {
            }
        })
    }

    override fun getItemCount(): Int {
        return checkListModelList.size;
    }

    public fun getList(): ArrayList<CheckListModelArray>
    {
        return checkListModelList
    }
}

open class MyHolder(view: View) : RecyclerView.ViewHolder(view) {
    var name = view.name
    var price = view.price
    var tax = view.tax
    var iv_delete = view.iv_delete
}