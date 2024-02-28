package com.trava.user.ui.home.payment

import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView
import com.trava.utilities.webservices.models.LoginModel
import com.trava.utilities.webservices.models.Service
import com.google.android.gms.maps.model.LatLng
import com.trava.user.webservices.models.ServiceRequestModel
import com.trava.user.webservices.models.cards_model.AddCardResponse
import com.trava.user.webservices.models.contacts.RideShareResponse
import com.trava.user.webservices.models.homeapi.ServiceDetails
import org.json.JSONObject

interface SavingCardContract {

    interface View: BaseView {
        fun onAddCardApiSuccess(data : AddCardResponse)
        fun onRemoveCardSuccess()
        fun onCreditPointsSucess(response : RideShareResponse)


    }

    interface Presenter: BasePresenter<View> {
        fun addCardApi()
        fun removeCardApi(userCardId : Int)
        fun requestUserCreditPoints()
    }
}