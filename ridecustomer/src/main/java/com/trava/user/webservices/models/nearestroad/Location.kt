package com.trava.user.webservices.models.nearestroad

import com.google.gson.annotations.SerializedName

data class Location(

	@field:SerializedName("latitude")
	val latitude: Double? = null,

	@field:SerializedName("longitude")
	val longitude: Double? = null
)