package com.trava.user.webservices.models.promocodes

import com.google.gson.annotations.SerializedName

data class PromoCodeResponse(

	@field:SerializedName("coupons")
	val coupons: ArrayList<CouponsItem> = ArrayList()
)