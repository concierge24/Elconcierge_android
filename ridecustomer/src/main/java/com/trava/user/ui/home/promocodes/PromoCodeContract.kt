package com.trava.user.ui.home.promocodes

import com.trava.user.webservices.models.promocodes.CouponsItem
import com.trava.user.webservices.models.promocodes.PromoCodeResponse
import com.trava.user.webservices.models.travelPackages.PackagesItem
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView

class PromoCodeContract{

    interface View: BaseView{
        fun onApiSuccess(data : PromoCodeResponse)
        fun onApiCheckPromoSuccess(data : CouponsItem)
    }

    interface Presenter: BasePresenter<View>{
        fun requestPromoCodesapiCall()
        fun checkPromoCodeApiCall(code : String)
    }
}