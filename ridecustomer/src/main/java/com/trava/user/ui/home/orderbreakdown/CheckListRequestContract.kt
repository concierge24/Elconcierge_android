package com.trava.user.ui.home.orderbreakdown

import com.google.gson.JsonArray
import com.trava.user.webservices.models.CheckListModelUpdate
import com.trava.user.webservices.models.order.CheckListModelArray
import com.trava.user.webservices.models.order.Order
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView
import org.json.JSONArray

class InvoiceRequestContract
{
    interface View: BaseView {
        fun onApiSuccess(response: List<Order>?)
        fun ApiSuccessDelete()
        fun ApiSuccessUpdate()
    }

    interface Presenter: BasePresenter<View> {
        fun onGoingOrderApi()
        fun deleteReqApi(ids:String)
        fun updataReqApi(check_lists: JSONArray)
    }
}