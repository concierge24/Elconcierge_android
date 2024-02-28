package com.trava.user.ui.menu.mygifts

import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.models.gift.CRequest
import com.trava.user.webservices.models.gift.ResultItem
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView

class MyGiftContract {
    interface View : BaseView {
        fun onReceivedGiftSuccess(listGifts: ArrayList<ResultItem?>)
        fun onSentGiftSuccess(listGifts: ArrayList<ResultItem?>)
        fun acceptRejectGiftSuccess(response: ApiResponse<Any>)
    }

    interface Presenter : BasePresenter<MyGiftContract.View> {
        fun getReceivedGiftList(take: Int, skip: Int)
        fun getSentGiftList(take: Int, skip: Int)
        fun acceptRejectGiftSuccess(order_id: String, skip: String)
    }
}