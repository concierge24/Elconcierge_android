package com.trava.user.ui.menu.editprofile

import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView
import com.trava.utilities.webservices.models.AppDetail
import java.io.File

class EditProfileContract{

    interface View: BaseView{
        fun onApiSuccess(response: AppDetail?)
    }

    interface Presenter: BasePresenter<View>{
        fun updateProfileApiCall(name: String?,email : String, phoneNumber : String,phoneCode : String, profilePicFile: File?)
    }

}