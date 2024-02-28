package com.trava.utilities.chatModel

data class UserId(
        val _id: String,
        val fullName: String,
        val firstName: String,
        val lastName: String,
        val emailId: String,
        val profilePicURL: ProfilePicUrl,
        var  phoneNo:String,
        var price:String

)