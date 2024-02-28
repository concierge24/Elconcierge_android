package com.trava.user.webservices.models.promocodes

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CouponsItem(

	@field:SerializedName("amount_value")
	val amountValue: String? = null,

	@field:SerializedName("code")
	val code: String? = null,

	@field:SerializedName("minimum_value")
	val minimum_value: String? = null,

	@field:SerializedName("maximum_discount")
	val maximum_discount: String? = null,

	@field:SerializedName("rides_value")
	val ridesValue: Int? = null,

	@field:SerializedName("expires_at")
	val expiresAt: String? = null,

	@field:SerializedName("coupon_id")
	val couponId: Int? = null,

	@field:SerializedName("price")
	val price: Int? = null,

	@field:SerializedName("purchase_counts")
	val purchaseCounts: Int? = null,

	@field:SerializedName("coupon_type")
	val couponType: String? = null,

	@field:SerializedName("blocked")
	val blocked: String? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("user_id")
	val userId: String? = null,

	@field:SerializedName("expiry_days")
	val expiryDays: Int? = null
) : Parcelable