package com.trava.user.webservices.models

import com.google.gson.annotations.SerializedName

data class CheckListResponseModel (
    @SerializedName("success") val success : Int,
    @SerializedName("statusCode") val statusCode : Int,
    @SerializedName("msg") val msg : Msg
    )