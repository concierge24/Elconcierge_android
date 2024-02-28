package com.trava.user.ui.home.promocodes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.trava.user.R
import com.trava.user.utils.ConfigPOJO
import com.trava.user.webservices.models.promocodes.CouponsItem
import com.trava.utilities.COUPEN_TYPE
import com.trava.utilities.Constants
import com.trava.utilities.DateUtils
import kotlinx.android.synthetic.main.item_promocodes.view.*

class PromoCodeAdapter(private var list : ArrayList<CouponsItem>, private var listener : PromoCodeInterface) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PromoViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_promocodes,parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is PromoViewHolder){
            holder.onBind(list[position])
        }
    }

    inner class PromoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        init {
            itemView.setOnClickListener {
                listener.selectedPromoCode(list[adapterPosition])
            }
        }

        fun onBind(data : CouponsItem) = with(itemView){
            tvPromoCode.text = data.code
            when(data.couponType){
                COUPEN_TYPE.VALUE -> {
                    tvPromoName.text = String.format("%s","${context.getString(R.string.get)} ${ConfigPOJO.currency+data.amountValue} ${context.getString(R.string.offFor)} ${data.ridesValue} ${context.getString(R.string.rides)+"."}")
                }
                COUPEN_TYPE.PERCENTAGE -> {
                    tvPromoName.text = String.format("%s","${context.getString(R.string.get)} ${data.amountValue+"%"} ${context.getString(R.string.offFor)} ${data.ridesValue} ${context.getString(R.string.rides)+"."}")
                }

            }
            tvExpires.text = String.format("%s","${context.getString(R.string.expires)} ${DateUtils.getFormattedTimeForPromoCodes(data.expiresAt?:"")}")
        }
    }
}

interface PromoCodeInterface {
    fun selectedPromoCode(data : CouponsItem)
}