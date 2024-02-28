package com.trava.user.ui.signup.entername

import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView
import com.trava.utilities.webservices.models.AppDetail
import java.io.File

class EnterNameContract{

    interface View: BaseView{
        fun onApiSuccess(response: AppDetail?)
    }

    interface Presenter: BasePresenter<View>{
        fun addNameApiCall(firstName: String, lastName: String, gender: String,
                           address: String, email: String, refCode: String, nationalID: String,
                           f_name: String, f_number: String, n_name: String, n_number: String,
                           pathFrontImagee: File, pathBackImagee: File, pathSchoolIdImage: File,
                           pathAddressProofImage: File, pathProfileImage: File)
    }

}