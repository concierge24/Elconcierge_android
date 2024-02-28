package com.trava.driver.ui.home.contactUs

import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView

class ContactUsContract{

    interface View: BaseView{
        fun onApiSuccess()
    }

    interface Presenter: BasePresenter<View>{
        fun sendMsg(msg: String)
    }
}