package com.trava.user.webservices.models.roadPickup

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserDriverDetailProductsItem(

	@field:SerializedName("price_per_sq_mt")
	val pricePerSqMt: Int? = null,

	@field:SerializedName("category_brand_product_id")
	val categoryBrandProductId: Int? = null,

	@field:SerializedName("price_per_hr")
	val pricePerHr: Int? = null,

	@field:SerializedName("deleted")
	val deleted: String? = null,

	@field:SerializedName("user_id")
	val userId: Int? = null,

	@field:SerializedName("price_per_weight")
	val pricePerWeight: Int? = null,

	@field:SerializedName("user_detail_id")
	val userDetailId: Int? = null,

	@field:SerializedName("price_per_quantity")
	val pricePerQuantity: Int? = null,

	@field:SerializedName("alpha_price")
	val alphaPrice: Int? = null,

	@field:SerializedName("price_per_distance")
	val pricePerDistance: Int? = null,

	@field:SerializedName("user_driver_detail_product_id")
	val userDriverDetailProductId: Int? = null
) : Parcelable