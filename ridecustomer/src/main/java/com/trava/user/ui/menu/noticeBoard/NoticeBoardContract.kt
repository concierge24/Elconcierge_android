package com.trava.user.ui.menu.noticeBoard

import com.trava.user.webservices.models.noticeBoard.NotificationData
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView

class NoticeBoardContract{

    interface View: BaseView{
        fun onApiSuccess(response: List<NotificationData>?)
    }

    interface Presenter: BasePresenter<View>{
        fun getNotifications()
    }

}