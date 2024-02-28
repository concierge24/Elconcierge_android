package com.trava.user.ui.home.chatModule.chatMessage

import com.trava.utilities.chatModel.ChatMessageListing
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView

class ChatMessageContract {
    interface View : BaseView {
        fun chatMessagesApiSuccess(chatList: ArrayList<ChatMessageListing>?, messageOrder: String)
    }

    interface Presenter : BasePresenter<View> {
        fun getChatMessagesApiCall(ordeID:String,messageId: String, receiverId: String, limit: Int, skip: Int, messageOrder: String)
    }
}