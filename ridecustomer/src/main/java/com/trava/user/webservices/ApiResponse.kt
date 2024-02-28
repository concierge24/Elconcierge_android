package com.trava.user.webservices

import com.google.gson.annotations.SerializedName

class ApiResponse<T> {
    val success: Int? = null
    @SerializedName("statusCode",alternate = ["status"])
    val statusCode: Int? = null
    @SerializedName("msg",alternate = ["message"])
    val msg: String? = null
    @SerializedName("result")
    val result: T? = null
}

class ApiResponseNew {
    val success: Int? = null
    val statusCode: Int? = null
    val msg: String? = null
}

class ApiResponseReq {
    val success: Int? = null
    val statusCode: Int? = null
}