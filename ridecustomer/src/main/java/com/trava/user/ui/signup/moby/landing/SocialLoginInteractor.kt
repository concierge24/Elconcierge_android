package com.trava.user.ui.signup.moby.landing

import com.trava.user.ui.signup.login.LoginContract
import com.trava.user.webservices.moby.response.UserExistResult
import com.trava.user.webservices.models.SendOtp
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView
import com.trava.utilities.webservices.models.LoginModel

class SocialLoginInteractor {

    interface View: BaseView {
        fun onSocialLoginApiSuccess(response: LoginModel?, map: Map<String, String>)
    }

    interface Presenter: BasePresenter<SocialLoginInteractor.View>{
        fun socialLogin(map: Map<String, String>)
    }
}