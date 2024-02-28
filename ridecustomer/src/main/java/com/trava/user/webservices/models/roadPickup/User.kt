package com.trava.user.webservices.models.roadPickup

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(

	@field:SerializedName("address_latitude")
	val addressLatitude: Double? = null,

	@field:SerializedName("lastName")
	val lastName: String? = null,

	@field:SerializedName("stripe_connect_id")
	val stripeConnectId: String? = null,

	@field:SerializedName("address")
	val address: String? = null,

	@field:SerializedName("gender")
	val gender: String? = null,

	@field:SerializedName("iso")
	val iso: String? = null,

	@field:SerializedName("stripe_connect_token")
	val stripeConnectToken: String? = null,

	@field:SerializedName("organisation_id")
	val organisationId: Int? = null,

	@field:SerializedName("stripe_customer_id")
	val stripeCustomerId: String? = null,

	@field:SerializedName("firstName")
	val firstName: String? = null,

	@field:SerializedName("password")
	val password: String? = null,

	@field:SerializedName("buraq_percentage")
	val buraqPercentage: Int? = null,

	@field:SerializedName("user_id")
	val userId: Int? = null,

	@field:SerializedName("referral_code")
	val referralCode: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("address_longitude")
	val addressLongitude: Double? = null,

	@field:SerializedName("bottle_charge")
	val bottleCharge: String? = null,

	@field:SerializedName("phone_number")
	val phoneNumber: Long? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("phone_code")
	val phoneCode: String? = null
) : Parcelable