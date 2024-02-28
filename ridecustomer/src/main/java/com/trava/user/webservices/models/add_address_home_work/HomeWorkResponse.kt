package com.trava.user.webservices.models.add_address_home_work

import com.google.gson.annotations.SerializedName

data class HomeWorkResponse(

	@field:SerializedName("address_latitude")
	val addressLatitude: String? = null,

	@field:SerializedName("access_token")
	val accessToken: String? = null,

	@field:SerializedName("app_language_id")
	val appLanguageId: String? = null,

	@field:SerializedName("address")
	val address: String? = null,

	@field:SerializedName("Details")
	val details: Details? = null,

	@field:SerializedName("user_id")
	val userId: Int? = null,

	@field:SerializedName("user_detail_id")
	val userDetailId: Int? = null,

	@field:SerializedName("address_longitude")
	val addressLongitude: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("user_type_id")
	val userTypeId: Int? = null,

	@field:SerializedName("user_organisation_id")
	val userOrganisationId: Int? = null,

	@field:SerializedName("category")
	val category: String? = null,

	@field:SerializedName("address_name")
	val addressName: String? = null
)