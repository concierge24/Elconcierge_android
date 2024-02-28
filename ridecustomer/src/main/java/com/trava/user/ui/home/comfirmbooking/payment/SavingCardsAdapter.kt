package com.trava.user.ui.home.comfirmbooking.payment

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.trava.user.R
import com.trava.user.webservices.models.CardModel
import kotlinx.android.synthetic.main.item_cards.view.*

class SavingCardsAdapter(private val savedcardInterface: SavedcardInterface)
    : RecyclerView.Adapter<SavingCardsAdapter.CardViewHolder>() {
    private var list = ArrayList<CardModel>()

    fun refreshList(list: ArrayList<CardModel>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        return CardViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_cards, parent, false))
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
            itemView.cvMain?.setOnClickListener(this)
            itemView.ivDelete.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            when (view?.id) {
                R.id.ivDelete -> {
                    if (adapterPosition != -1) {
                        savedcardInterface.getSavedCardData("deleteCard", adapterPosition, list[adapterPosition])
                    }
                }
                R.id.cvMain -> {
                    if (adapterPosition != -1) {
                        savedcardInterface.getSavedCardData("selectForPayment", adapterPosition, list[adapterPosition])
                    }
                }
            }
        }

        fun onBind(data: CardModel) = with(itemView) {
            tvCardHolderName.text = data.card_holder_name

            if (data.card_no == null) {
                tvCardNo.text = "**** **** **** " + data.last4
            } else {
                tvCardNo.text = String.format("%s", "${data.card_no}")
            }

            if(!TextUtils.isEmpty(data.exp_month)){
                tvExpiryDate.text = String.format("%s", "${data.exp_month}${"/"}${data.exp_year}")
            }else{
                tvExpiryDate.visibility = View.GONE
                tvValidUpto.visibility = View.GONE
            }

            if (data.isSelected == true) {
                ivDefault.visibility = View.VISIBLE
                ivDefault.setImageResource(R.drawable.ic_check_1)
            } else {
                ivDefault.visibility = View.GONE
            }

            when (data.card_brand) {
                "Visa" -> ivCardType.setImageResource(R.drawable.ic_visa_new)
                "American Express" -> ivCardType.setImageResource(R.drawable.ic_amex)
                "Discover" -> ivCardType.setImageResource(R.drawable.ic_discover)
                "JCB" -> ivCardType.setImageResource(R.drawable.ic_jcb)
                "MasterCard" -> ivCardType.setImageResource(R.drawable.ic_mastercard)
                "Diners Club" -> ivCardType.setImageResource(R.drawable.ic_diners)
                "elo" -> ivCardType.setImageResource(R.drawable.ic_elo)
                "Hipercard" -> ivCardType.setImageResource(R.drawable.ic_hiper)
                "UnionPay" -> ivCardType.setImageResource(R.drawable.ic_up)
            }
        }
    }
}

interface SavedcardInterface {
    fun getSavedCardData(actionType: String, adapterPosition: Int, cardData: CardModel)
}