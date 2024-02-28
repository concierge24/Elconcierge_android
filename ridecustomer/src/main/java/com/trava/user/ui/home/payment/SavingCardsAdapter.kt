package com.trava.user.ui.home.payment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.trava.user.R
import com.trava.user.webservices.models.cards_model.UserCardData
import com.trava.utilities.Constants
import kotlinx.android.synthetic.main.item_cards.view.*


/**
 * Created by aseem4 on 14/11/18.
 */
class SavingCardsAdapter(private val savedcardInterface: SavedcardInterface) : RecyclerView.Adapter<SavingCardsAdapter.CardViewHolder>() {
    private var list = ArrayList<UserCardData>()

    fun refreshList(list: ArrayList<UserCardData>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavingCardsAdapter.CardViewHolder {
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
                        savedcardInterface.getSavedCardData(Constants.DELETE_CARD, adapterPosition, list[adapterPosition])
                    }
                }
                R.id.cvMain -> {
                    if (adapterPosition != -1) {
                        savedcardInterface.getSavedCardData(Constants.MAKE_PAYMENT, adapterPosition, list[adapterPosition])
                    }
                }
            }


        }


        fun onBind(data: UserCardData) = with(itemView) {
            tvCardHolderName.text = data.cardHolderName
            tvCardNo.text = String.format("%s", "${data.cardNo}")
            tvExpiryDate.text = String.format("%s", "${data.cardExpiry?.substring(2)}${"/"}${data.cardExpiry?.substring(2,4)}")
//            if (data.defaultStatus == 1) {
//                ivDefault.visibility = View.VISIBLE
//                ivDefault.setImageResource(R.drawable.ic_check_1)
//            } else {
//                ivDefault.visibility = View.GONE
//            }
//            when (data.typeOfCard) {
//                "Visa" -> ivCardType.setImageResource(R.drawable.ic_visa_new)
//                "American Express" -> ivCardType.setImageResource(R.drawable.ic_amex)
//                "Discover" -> ivCardType.setImageResource(R.drawable.ic_discover)
//                "JCB" -> ivCardType.setImageResource(R.drawable.ic_jcb)
//                "MasterCard" -> ivCardType.setImageResource(R.drawable.ic_mastercard)
//                "Diners Club" -> ivCardType.setImageResource(R.drawable.ic_diners)
//                "elo" -> ivCardType.setImageResource(R.drawable.ic_elo)
//                "Hipercard" -> ivCardType.setImageResource(R.drawable.ic_hiper)
//                "UnionPay" -> ivCardType.setImageResource(R.drawable.ic_up)
//            }
        }

    }
}

interface SavedcardInterface {
    fun getSavedCardData(actionType: String, adapterPosition: Int, cardData: UserCardData)
}