package com.trava.user.webservices.models.travelPackages

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.trava.user.webservices.models.homeapi.Product
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CategoryBrandsItem(

	@field:SerializedName("category_brand_id")
	val categoryBrandId: Int? = null,

	@field:SerializedName("category_id")
	val categoryId: Int? = null,

	@field:SerializedName("buraq_percentage")
	val buraqPercentage: Int? = null,

	@field:SerializedName("blocked")
	val blocked: String? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("sort_order")
	val sortOrder: Int? = null,

	@field:SerializedName("category_brand_type")
	val categoryBrandType: String? = null,

	@field:SerializedName("products")
	var products: ArrayList<Product> = ArrayList()
) : Parcelable