package com.trava.user.webservices.moby.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PvtCooperativeRegResponseModel {
    @SerializedName("type")
    @Expose
    private var type: String? = null
    @SerializedName("access_token")
    @Expose
    private var accessToken: String? = null
    @SerializedName("otp")
    @Expose
    private var otp: String? = null


    fun getType(): String? {
        return type
    }

    fun setType(type: String?) {
        this.type = type
    }

    fun getAccessToken(): String? {
        return accessToken
    }

    fun setAccessToken(accessToken: String?) {
        this.accessToken = accessToken
    }

    fun getOtp(): String? {
        return otp
    }

    fun setOtp(otp: String?) {
        this.otp = otp
    }

}