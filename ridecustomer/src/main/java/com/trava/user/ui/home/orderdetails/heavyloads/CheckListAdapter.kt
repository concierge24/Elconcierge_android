package com.trava.user.ui.home.orderdetails.heavyloads

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.trava.user.R
import com.trava.user.databinding.ChecklistItemlayoutBinding
import com.trava.user.utils.ConfigPOJO
import com.trava.user.webservices.models.CheckListModel
import kotlinx.android.synthetic.main.checklist_itemlayout.view.*

class CheckListAdapter(var mContext: Context?, val checkListModelList: ArrayList<CheckListModel>,
                       val  onInfoCallBack : OnInfocallBack) : RecyclerView.Adapter<CheckListMyHolder>() {

    var state: String? = null

    open interface OnInfocallBack
    {
        fun onInfoClickDelete(vi : View,position: Int)
        fun onInfoClickEdit(tModelList: ArrayList<CheckListModel>)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckListMyHolder {
        var binding: ChecklistItemlayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.checklist_itemlayout, parent, false)
        binding.color = ConfigPOJO.Companion
        return CheckListMyHolder(binding.root)
    }

    override fun onBindViewHolder(holder: CheckListMyHolder, position: Int) {
        holder.name.setText(checkListModelList.get(position).item_name)
        holder.price.setText(checkListModelList.get(position).price.toString())

        holder.iv_delete.setOnClickListener {
            onInfoCallBack.onInfoClickDelete(it,position)
        }

        holder.price.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s:CharSequence, start:Int, count:Int, after:Int) {
            }
            override fun onTextChanged(s:CharSequence, start:Int, before:Int, count:Int) {
                checkListModelList.get(position).price=s.toString()
                onInfoCallBack.onInfoClickEdit(checkListModelList)
            }
            override fun afterTextChanged(s: Editable) {
            }
        })
    }

    override fun getItemCount(): Int {
        return checkListModelList.size;
    }

    public fun getList(): ArrayList<CheckListModel>
    {
        return checkListModelList
    }
}

open class CheckListMyHolder(view: View) : RecyclerView.ViewHolder(view) {
    var name = view.name
    var price = view.price
    var iv_delete = view.iv_delete
}