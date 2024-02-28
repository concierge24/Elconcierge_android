package com.trava.user.ui.home.chatModule

import com.trava.utilities.chatModel.ChatMessageListing
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView

class ChatContract{

    interface View : BaseView {
        fun chatLogsApiSuccess(chatList: ArrayList<ChatMessageListing>?)
    }

    interface Presenter : BasePresenter<View> {
        fun getChatLogsApiCall(authorization : String , limit: Int, skip: Int)
    }
}