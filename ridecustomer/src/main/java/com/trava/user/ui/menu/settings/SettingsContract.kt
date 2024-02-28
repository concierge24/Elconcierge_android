package com.trava.user.ui.menu.settings

import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView

class SettingsContract{

    interface View: BaseView{
        fun onSettingsApiSuccess()
    }

    interface Presenter: BasePresenter<View>{
        fun updateSettingsApiCall(languageId: String?, notificationFlag: String?)
    }
}