package com.trava.user.ui.home.comfirmbooking.outstanding_payment

import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView
import com.trava.utilities.webservices.models.LoginModel
import com.trava.utilities.webservices.models.Service
import com.google.android.gms.maps.model.LatLng
import com.trava.user.webservices.models.ServiceRequestModel
import com.trava.user.webservices.models.homeapi.ServiceDetails
import org.json.JSONObject

interface OutstandingPaymentContract {

    interface View: BaseView {
        fun outStandingPaymentSuccess()

    }

    interface Presenter: BasePresenter<View> {
        fun outStandingPaymentByCard(userCardId : Int,amount : Double)

    }

}