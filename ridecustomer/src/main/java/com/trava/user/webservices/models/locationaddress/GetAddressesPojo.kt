package com.trava.user.webservices.models.locationaddress

import com.google.gson.annotations.SerializedName

data class GetAddressesPojo(

	@field:SerializedName("msg")
	val msg: String? = null,

	@field:SerializedName("result")
	val result: List<ResultItem?>? = null,

	@field:SerializedName("success")
	val success: Int? = null,

	@field:SerializedName("statusCode")
	val statusCode: Int? = null
)

data class ResultItem(

	@field:SerializedName("country")
	val country: String? = null,

	@field:SerializedName("createdAt")
	val createdAt: String? = null,

	@field:SerializedName("address")
	val address: String? = null,

	@field:SerializedName("blocked")
	val blocked: String? = null,

	@field:SerializedName("city")
	val city: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("state")
	val state: String? = null,

	@field:SerializedName("zip_code")
	val zipCode: Int? = null,

	@field:SerializedName("updatedAt")
	val updatedAt: String? = null,

	@field:SerializedName("latitude")
	val latitude: String? = null,

	@field:SerializedName("longitude")
	val longitude: String? = null
)
