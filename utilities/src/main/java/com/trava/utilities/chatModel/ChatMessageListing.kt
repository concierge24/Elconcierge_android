package com.trava.utilities.chatModel

data class ChatMessageListing(
        var c_id: String?,
        var message_id: String?,
        var send_to: String?,
        var send_by: String?,
        var text: String?,
        var thumbnail_image: String?,
        var sent_at: String,
        var chat_type: String?,
        var isSent: Boolean?,
        var original_image: String?,
        var name: String?,
        var profile_pic: String?,
        var oppositionId: String?,
        var isFailed: Boolean? = false,
        var user_detail_id: String? = null,
        var order_id: String? = null
)