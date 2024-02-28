package com.trava.user.ui.home.processingrequest

import com.trava.user.webservices.models.order.Order
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView
import java.math.BigInteger

class ProcessingRequestContract{

    interface View: BaseView{
        fun onApiSuccess()
        fun handleOrderError(order: Order?)
        fun onCancelHalfWayStopSucess()
        fun onCancelVehicleBreakdownSuccess()
    }

    interface Presenter: BasePresenter<View>{
        fun requestCancelApiCall(map: HashMap<String, String>)
        fun requestHafWaycancelByUser(orderId : BigInteger)
        fun requestVehicleBreakDownCancelByUser(orderId : BigInteger)

    }
}