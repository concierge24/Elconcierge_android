package com.trava.user.webservices.models.order

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CouponDetail(
	@field:SerializedName("amount_value")
	val amountValue: String,

	@field:SerializedName("code")
	val code: String,

	@field:SerializedName("created_at")
	val createdAt: String,

	@field:SerializedName("minimum_value")
	val minimum_value: String? = null,

	@field:SerializedName("is_referral")
	val isReferral: Boolean,

	@field:SerializedName("rides_value")
	val ridesValue: Int,

	@field:SerializedName("expires_at")
	val expiresAt: String,

	@field:SerializedName("coupon_id")
	val couponId: Int,

	@field:SerializedName("blocked")
	val blocked: String,

	@field:SerializedName("updated_at")
	val updatedAt: String,

	@field:SerializedName("user_id")
	val userId: Int,

	@field:SerializedName("price")
	val price: Int,

	@field:SerializedName("coupon_type")
	val couponType: String,

	@field:SerializedName("expiry_days")
	val expiryDays: Int
) : Parcelable
