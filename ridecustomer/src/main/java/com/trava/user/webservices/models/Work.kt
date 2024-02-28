package com.trava.user.webservices.models

import com.google.gson.annotations.SerializedName

data class Work(
		@field:SerializedName("address_latitude")
		val addressLatitude: Double? = null,

		@field:SerializedName("address")
		val address: String? = null,

		@field:SerializedName("address_name")
		val addressName: String? = null,

		@field:SerializedName("blocked")
		val blocked: String? = null,

		@field:SerializedName("updated_at")
		val updatedAt: String? = null,

		@field:SerializedName("user_id")
		val userId: Int? = null,

		@field:SerializedName("user_address_id")
		val userAddressId: Int? = null,

		@field:SerializedName("address_longitude")
		val addressLongitude: Double? = null,

		@field:SerializedName("created_at")
		val createdAt: String? = null,

		@field:SerializedName("category")
		val category: String? = null
)