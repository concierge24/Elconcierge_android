package com.trava.user.ui.signup.moby.resgister_screens.fragments.institution

import com.trava.user.ui.menu.settings.editprofile.EditProfileContract
import com.trava.user.ui.signup.moby.resgister_screens.fragments.login.EmailLoginContractor
import com.trava.user.webservices.moby.response.PvtCooperativeRegResponseModel
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView
import com.trava.utilities.webservices.models.AppDetail
import com.trava.utilities.webservices.models.LoginModel
import java.io.File

class PvyCooperativeFirmContarctor {
    interface View: BaseView {
        fun onApiSuccess(response: PvtCooperativeRegResponseModel?)
    }

    interface Presenter: BasePresenter<View> {
        fun pvyCooperariveRegister(cooperation_id : String?,identification_number : String?,
                                   email : String,phone_code : String
                                    ,iso : String,phone_number :String,password :String,
                                   timezone :String, latitude :String,longitude :String,document: File?)
    }

}