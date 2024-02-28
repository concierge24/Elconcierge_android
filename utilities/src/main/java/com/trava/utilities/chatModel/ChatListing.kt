package com.trava.utilities.chatModel


data class ChatListing(
        var c_id: String?,
        var message_id: String?,
        var send_to: String?,
        var send_by: String?,
        var text: String?,
        var chatImage: ProfilePicUrl?,
        var sent_at: String,
        var chatType: String?,
        var isSent: Boolean?,
        var isDeliver: Boolean?,
        var isFailed: Boolean? = false,
        var mediaToUpload: MediaUpload? = null
)