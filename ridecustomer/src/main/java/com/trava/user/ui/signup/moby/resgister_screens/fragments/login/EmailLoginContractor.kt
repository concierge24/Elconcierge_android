package com.trava.user.ui.signup.moby.resgister_screens.fragments.login

import com.trava.user.ui.signup.moby.landing.SocialLoginInteractor
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView
import com.trava.utilities.webservices.models.LoginModel

class EmailLoginContractor {
    interface View: BaseView {
        fun onApiSuccess(response: LoginModel?, map: Map<String, String>)
    }

    interface Presenter: BasePresenter<EmailLoginContractor.View> {
        fun emailLogin(map: Map<String, String>)
    }
}