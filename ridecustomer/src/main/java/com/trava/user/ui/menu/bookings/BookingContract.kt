package com.trava.user.ui.menu.bookings

import com.trava.user.webservices.models.order.Order
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView

class BookingContract {

    interface View : BaseView {
        fun onApiSuccess(listBookings: ArrayList<Order?>)
        fun onOrderDetailsSuccess(response: Order?)
        fun onCancelApiSuccess()
        fun onFilterSuccess()
    }

    interface Presenter : BasePresenter<View> {
        fun getHistoryList(take: Int, skip: Int, type: Int,startDate : String, endDate : String,category_id:String)
        fun getOrderDetails(orderId: Long)
        fun requestCancelApiCall(map: HashMap<String, String>)
    }

}