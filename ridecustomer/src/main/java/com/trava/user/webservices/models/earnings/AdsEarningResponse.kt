package com.trava.user.webservices.models.earnings

import com.google.gson.annotations.SerializedName

data class AdsEarningResponse(
	@field:SerializedName("msg")
	val msg: String? = null,

	@field:SerializedName("results")
	val results: List<ResultItem?>? = null,

	@field:SerializedName("success")
	val success: Int? = null,

	@field:SerializedName("statusCode")
	val statusCode: Int? = null,

	@field:SerializedName("total")
	val total: Int? = null
)

data class ResultItem(
	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("earning")
	val earning: Int? = null
)
