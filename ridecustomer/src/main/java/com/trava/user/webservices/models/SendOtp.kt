package com.trava.user.webservices.models

data class SendOtp(
        var type: String,
        var access_token: String,
        var otp: Int,
        var Versioning: Versioning
)