package com.trava.user.ui.menu.earnings

import com.trava.user.webservices.models.earnings.AdsEarningResponse
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView

class EarningContract {
    interface View: BaseView {
        fun onEarningAiSuccess(responseData: AdsEarningResponse)
    }

    interface Presenter: BasePresenter<View> {
        fun requestEarnings(map:HashMap<String,Any>)
    }
}