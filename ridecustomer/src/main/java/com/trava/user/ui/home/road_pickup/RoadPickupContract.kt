package com.trava.user.ui.home.road_pickup

import com.trava.user.webservices.models.order.Order
import com.trava.user.webservices.models.roadPickup.RoadPickupResponse
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView

class RoadPickupContract{

    interface View: BaseView{
        fun onApiSuccess(responseData : RoadPickupResponse)
    }

    interface Presenter: BasePresenter<View>{
        fun requestGetDataFromQRCodeApiCall(qrCodeScanUserId: String)
    }
}