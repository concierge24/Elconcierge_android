package com.trava.user.webservices.moby.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class UserExistResponseModel {
    @SerializedName("success")
    @Expose
    private var success: Int? = null
    @SerializedName("statusCode")
    @Expose
    private var statusCode: Int? = null
    @SerializedName("result")
    @Expose
    private var result: UserExistResult? = null

    fun getSuccess(): Int? {
        return success
    }

    fun setSuccess(success: Int?) {
        this.success = success
    }

    fun getStatusCode(): Int? {
        return statusCode
    }

    fun setStatusCode(statusCode: Int?) {
        this.statusCode = statusCode
    }

    fun getResult(): UserExistResult? {
        return result
    }

    fun setResult(result: UserExistResult?) {
        this.result = result
    }
}