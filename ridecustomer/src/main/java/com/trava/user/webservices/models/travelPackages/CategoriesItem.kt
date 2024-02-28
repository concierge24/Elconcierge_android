package com.trava.user.webservices.models.travelPackages

import com.google.gson.annotations.SerializedName

data class CategoriesItem(

	@field:SerializedName("default_brands")
	val defaultBrands: String? = null,

	@field:SerializedName("image")
	val image: String? = null,

	@field:SerializedName("maximum_distance")
	val maximumDistance: String? = null,

	@field:SerializedName("category_id")
	val categoryId: Int? = null,

	@field:SerializedName("buraq_percentage")
	val buraqPercentage: Int? = null,

	@field:SerializedName("blocked")
	val blocked: String? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("maximum_rides")
	val maximumRides: Int? = null,

	@field:SerializedName("category_type")
	val categoryType: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("sort_order")
	val sortOrder: Int? = null
)