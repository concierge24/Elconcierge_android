package com.trava.user.webservices.models

import com.google.gson.annotations.SerializedName

data class RecentItem(

	@field:SerializedName("address_latitude")
	var addressLatitude: Double? = null,

	@field:SerializedName("address")
	var address: String? = null,

	@field:SerializedName("address_name")
	var addressName: String? = null,

	@field:SerializedName("blocked")
	var blocked: String? = null,

	@field:SerializedName("updated_at")
	var updatedAt: String? = null,

	@field:SerializedName("user_id")
	var userId: Int? = null,

	@field:SerializedName("user_address_id")
	var userAddressId: Int? = null,

	@field:SerializedName("address_longitude")
	var addressLongitude: Double? = null,

	@field:SerializedName("created_at")
	var createdAt: String? = null,

	@field:SerializedName("category")
	var category: String? = null
)