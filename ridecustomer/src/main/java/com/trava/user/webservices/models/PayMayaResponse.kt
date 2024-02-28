package com.trava.user.webservices.models

import com.google.gson.annotations.SerializedName

data class PayMayaResponse(
        @field:SerializedName("data")
        val data: Data? = null,

        @field:SerializedName("success")
        val success: Int? = null,

        @field:SerializedName("message")
        val message: String? = null,

        @field:SerializedName("statusCode")
        val statusCode: Int? = null
)

data class Data(

        @field:SerializedName("redirectUrl")
        val redirectUrl: String? = null,

        @field:SerializedName("checkoutId")
        val checkoutId: String? = null
)


data class ThawaniData(
        val payment_url: String? = null
)

