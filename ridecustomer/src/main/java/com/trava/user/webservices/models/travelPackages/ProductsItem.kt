package com.trava.user.webservices.models.travelPackages

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProductsItem(

	@field:SerializedName("image")
	val image: String? = null,

	@field:SerializedName("category_brand_product_id")
	val categoryBrandProductId: Int? = null,

	@field:SerializedName("max_quantity")
	val maxQuantity: Int? = null,

	@field:SerializedName("seating_capacity")
	val seatingCapacity: Int? = null,

	@field:SerializedName("min_quantity")
	val minQuantity: Int? = null,

	var name: String?,
	var description: String?,
	var sort_order: Int?,
	var actual_value: Float?,
	var alpha_price: Float?,
	var price_per_quantity: Float?,
	var price_per_distance: Float?,
	var price_per_weight: Float?,
	val image_url: String? = null,
	var isSelected: Boolean? = false,
	var price_per_hr : Float
) : Parcelable