package com.trava.user.ui.home.comfirmbooking

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.trava.user.R
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.models.homeapi.HomeDriver
import com.trava.utilities.setRoundProfileUrl
import kotlinx.android.synthetic.main.item_cards.view.cvMain
import kotlinx.android.synthetic.main.item_cards.view.tvCardHolderName
import kotlinx.android.synthetic.main.item_cards.view.tvCardNo
import kotlinx.android.synthetic.main.item_driver.view.*

class DriverAdapter(private val savedcardInterface: SavedDriverInterface)
    : RecyclerView.Adapter<DriverAdapter.CardViewHolder>() {
    private var list = ArrayList<HomeDriver?>()

    fun refreshList(list: ArrayList<HomeDriver?>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        return CardViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_driver, parent, false))
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.onBind(list[position])

    }

    override fun getItemCount(): Int {
        return list.size

    }


    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        init {
            listener()
        }

        private fun listener() {
            itemView.tvSubmit_data?.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            when (view?.id) {
                R.id.tvSubmit_data -> {
                    if (adapterPosition != -1) {
                        savedcardInterface.getSavedCardData("deleteCard", adapterPosition, list[adapterPosition]!!)
                    }
                }
            }
        }

        fun onBind(data: HomeDriver?) = with(itemView) {
            tvCardHolderName.text = data?.name
            tvCardNo.text = data?.vehicle_number
            iv_profilee?.setRoundProfileUrl(data?.profile_pic)
            tvSubmit_data.background = StaticFunction.changeBorderTextColor(ConfigPOJO.Btn_Colour, ConfigPOJO.Btn_Colour, GradientDrawable.RECTANGLE)
        }
    }
}

interface SavedDriverInterface {
    fun getSavedCardData(actionType: String, adapterPosition: Int, cardData: HomeDriver)
}