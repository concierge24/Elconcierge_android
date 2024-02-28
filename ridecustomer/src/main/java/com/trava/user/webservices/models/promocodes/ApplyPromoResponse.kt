package com.trava.user.webservices.models.promocodes

import com.google.gson.annotations.SerializedName

data class ApplyPromoResponse(

	@field:SerializedName("amount_value")
	val amountValue: String? = null,

	@field:SerializedName("code")
	val code: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("referral_user_id")
	val referralUserId: Any? = null,

	@field:SerializedName("is_referral")
	val isReferral: Int? = null,

	@field:SerializedName("rides_value")
	val ridesValue: Int? = null,

	@field:SerializedName("expires_at")
	val expiresAt: String? = null,

	@field:SerializedName("coupon_id")
	val couponId: Int? = null,

	@field:SerializedName("blocked")
	val blocked: String? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("user_id")
	val userId: Any? = null,

	@field:SerializedName("price")
	val price: Int? = null,

	@field:SerializedName("coupon_type")
	val couponType: String? = null,

	@field:SerializedName("expiry_days")
	val expiryDays: Int? = null
)