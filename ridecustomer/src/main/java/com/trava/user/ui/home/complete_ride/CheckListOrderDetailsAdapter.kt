package com.trava.user.ui.home.complete_ride

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.trava.user.R
import com.trava.user.utils.ConfigPOJO
import com.trava.user.webservices.models.order.CheckListModelArray
import kotlinx.android.synthetic.main.checklist_detail_itemlayout.view.*

class CheckListDetailsAdapter(var mContext: Context?, val checkListModelList: ArrayList<CheckListModelArray>) : RecyclerView.Adapter<MyHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder =
            MyHolder(LayoutInflater.from(parent.context).inflate(R.layout.checklist_detail_itemlayout,parent,
                    false))

    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        holder.name.setText(checkListModelList.get(position).item_name)
        holder.price.setText(checkListModelList.get(position).after_item_price+" "+ ConfigPOJO.currency)
        holder.tax.setText(checkListModelList.get(position).tax.toString()+" "+ConfigPOJO.currency)
    }

    override fun getItemCount(): Int {
        return checkListModelList.size;
    }
}

open class MyHolder(view: View) : RecyclerView.ViewHolder(view) {
    var name = view.name
    var price = view.price
    var tax = view.tax
}