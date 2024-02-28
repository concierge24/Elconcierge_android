package com.trava.user.ui.home.comfirmbooking.payment

import com.trava.user.webservices.models.CardModel
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView

class PaymentContract{

    interface View: BaseView {
        fun onApiSuccess(response: CardModel?)
        fun onApiSuccess(response: ArrayList<CardModel>?)
        fun onApiSuccess(isDeleted : Boolean)
    }

    interface Presenter: BasePresenter<View> {
        fun saveCard(map: HashMap<String, Any>)
        fun getCards()
        fun deleteCards(map: HashMap<String, Any>)
    }
}