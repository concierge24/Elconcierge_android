package com.trava.user.webservices.models.contacts

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ContactModel(

		@field:SerializedName("phone_number")
		var phoneNumber: String? = null,

		@field:SerializedName("name")
		var name: String? = null,

		@field:SerializedName("unique_Id")
		val uniqueId: String? = null,

		@field:SerializedName("isSelected")
		var isSelected: Boolean? = false,

		@field:SerializedName("ride_shared_id")
		val rideSharedId: Int? = null,

		@field:SerializedName("updated_at")
		val updatedAt: String? = null,

		@field:SerializedName("cancelled")
		val cancelled: String? = null,

		@field:SerializedName("created_at")
		val createdAt: String? = null,

		@field:SerializedName("customer_user_id")
		val customerUserId: Int? = null,

		@field:SerializedName("order_id")
		val orderId: Int? = null,

		@field:SerializedName("phone_code")
		var phoneCode: String? = null,

		@field:SerializedName("creat 2019-12-16 12:37:33.453 29739-30474/com.trava.user D/OkHttp: ed_at")
		val creat201912161237334532973930474ComTravaUserDOkHttpEdAt: String? = null




) : Parcelable