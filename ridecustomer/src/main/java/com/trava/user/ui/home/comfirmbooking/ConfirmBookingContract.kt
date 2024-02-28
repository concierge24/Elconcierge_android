package com.trava.user.ui.home.comfirmbooking

import com.trava.driver.webservices.models.WalletBalModel
import com.trava.user.webservices.models.ProductCheckListModel
import com.trava.user.webservices.models.ProductListModel
import com.trava.user.webservices.models.ServiceRequestModel
import com.trava.user.webservices.models.contacts.RideShareResponse
import com.trava.user.webservices.models.order.Order
import com.trava.user.webservices.models.orderrequest.ResultItem
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView

class ConfirmBookingContract{

    interface View: BaseView{
        fun onApiSuccess(response: Order?)
        fun onOutstandingCharges(response: Order?)
        fun onWalletBalSuccess(response: WalletBalModel?)
        fun onCreditPointsSucess(response : RideShareResponse)
        fun onRequestApiSuccess(response : ResultItem)
    }

    interface Presenter: BasePresenter<View>{
        fun requestServiceApiCall(requestModel: ServiceRequestModel)
        fun getWalletBalance(isLoader: Boolean)
        fun requestUserCreditPoints()
        fun requestPickupApi(requestModel: ServiceRequestModel, productList: ArrayList<ProductListModel>, productCheckList: ArrayList<ProductCheckListModel>)
    }
}