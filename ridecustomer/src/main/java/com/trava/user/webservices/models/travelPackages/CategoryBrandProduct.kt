package com.trava.user.webservices.models.travelPackages

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CategoryBrandProduct(

	@field:SerializedName("image")
	val image: String? = null,

	@field:SerializedName("category_brand_product_id")
	val categoryBrandProductId: Int? = null,

	@field:SerializedName("max_quantity")
	val maxQuantity: Int? = null,

	@field:SerializedName("seating_capacity")
	val seatingCapacity: Int? = null,

	@field:SerializedName("min_quantity")
	val minQuantity: Int? = null
) : Parcelable