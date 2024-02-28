package com.trava.user.webservices.models.cards_model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserCardData(

	@field:SerializedName("deleted")
	val deleted: String? = null,

	@field:SerializedName("card_no")
	val cardNo: String? = null,

	@field:SerializedName("card_holder_name")
	val cardHolderName: String? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("user_card_id")
	val userCardId: Int? = null,

	@field:SerializedName("user_id")
	val userId: Int? = null,

	@field:SerializedName("payment_id")
	val paymentId: String? = null,

	@field:SerializedName("customer_token")
	val customerToken: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("order_id_payment")
	val orderIdPayment: String? = null,

	@field:SerializedName("is_default")
	val isDefault: String? = null,

	@field:SerializedName("card_expiry")
	val cardExpiry: String? = null
) : Parcelable