package com.trava.user.ui.signup.moby.resgister_screens.fragments.institution

import com.trava.user.ui.signup.moby.resgister_screens.fragments.login.EmailLoginContractor
import com.trava.user.webservices.moby.response.InstituteListing
import com.trava.user.webservices.moby.response.InstituteListingResponseModel
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView
import com.trava.utilities.webservices.models.LoginModel

class InstituteListingContractor  {
    interface View: BaseView {
        fun onApiSuccess(response: ArrayList<InstituteListing>?)
    }

    interface Presenter: BasePresenter<View> {
        fun getInstituteListing(items : String, cooperation_type : String)
    }
}