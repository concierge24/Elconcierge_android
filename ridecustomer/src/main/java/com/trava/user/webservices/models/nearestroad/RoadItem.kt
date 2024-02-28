package com.trava.user.webservices.models.nearestroad

import com.google.gson.annotations.SerializedName

data class RoadItem(

	@field:SerializedName("placeId")
	val placeId: String? = null,

	@field:SerializedName("originalIndex")
	val originalIndex: Int? = null,

	@field:SerializedName("location")
	val location: Location? = null
)