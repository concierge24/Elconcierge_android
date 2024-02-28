package com.trava.user.ui.signup.moby.landing

import com.trava.user.webservices.moby.response.UserExistResponseModel
import com.trava.user.webservices.moby.response.UserExistResult
import com.trava.user.webservices.models.SendOtp
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView

class UserExistanceConract {
    interface View: BaseView {
        fun onApiSuccess(response: UserExistResult?)
    }

    interface Presenter: BasePresenter<View> {
        fun checkUserExist( key : String , type : String )
    }
}