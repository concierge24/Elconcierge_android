package com.trava.user.ui.signup.verifytop

import com.trava.user.webservices.models.SendOtp
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView
import com.trava.utilities.webservices.models.LoginModel

class VerifyOtpContract{

    interface View: BaseView{
        fun onApiSuccess(response: LoginModel?)
        fun resendOtpSuccss(response: SendOtp?)
    }

    interface Presenter: BasePresenter<View>{
        fun verifyOtp(map: Map<String, String>)
        fun sendOtpApiCall(map: Map<String, String>)
    }
}