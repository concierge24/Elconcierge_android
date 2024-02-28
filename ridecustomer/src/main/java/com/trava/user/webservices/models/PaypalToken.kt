package com.trava.user.webservices.models

import com.google.gson.annotations.SerializedName

data class PaypalToken(

	@field:SerializedName("success")
	val success: Int? = null,

	@field:SerializedName("client_token")
	val clientToken: String? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("statusCode")
	val statusCode: Int? = null
)
