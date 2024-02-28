package com.trava.user.webservices.models.categories

import com.google.gson.annotations.SerializedName

data class BannerData(

	@field:SerializedName("image")
	val image: String? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("banner_id")
	val bannerId: Int? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("banner_url")
	val bannerUrl: String? = null,

	@field:SerializedName("title")
	val title: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)