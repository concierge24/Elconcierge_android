package com.trava.user.webservices.models.checkList

import com.trava.user.webservices.models.CheckListItem
import com.trava.user.webservices.models.CheckListModel
import com.trava.user.webservices.models.CheckListResponseModel
import com.trava.user.webservices.models.ServiceRequestModel
import com.trava.user.webservices.models.appsettings.SettingItems
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView

class CheckListcontractor {
    interface View: BaseView {
        fun onApiSuccess(response: CheckListItem?)
    }

    interface Presenter: BasePresenter<View>
    {
        fun saveCheckList( checkListModel : ArrayList<CheckListModel>)
    }
}
