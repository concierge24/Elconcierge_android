package com.trava.user.ui.signup.login

import com.trava.user.webservices.models.SendOtp
import com.trava.user.webservices.models.appsettings.LanguageSets
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView

class LoginContract{

    interface View: BaseView{
        fun onApiSuccess(response: SendOtp?, map: Map<String, String>)
        fun onSettingsApiSuccess()
        fun onSettingsApiSuccess(list :ArrayList<LanguageSets>?)
    }

    interface Presenter: BasePresenter<View>{
        fun sendOtpApiCall(map: Map<String, String>)
        fun updateSettingsApiCall(languageId: String?, notificationFlag: String?)
        fun getLanguages()
    }

}