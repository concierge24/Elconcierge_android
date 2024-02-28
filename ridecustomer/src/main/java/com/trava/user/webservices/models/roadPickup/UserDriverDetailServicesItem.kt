package com.trava.user.webservices.models.roadPickup

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserDriverDetailServicesItem(

	@field:SerializedName("deleted")
	val deleted: String? = null,

	@field:SerializedName("category_id")
	val categoryId: Int? = null,

	@field:SerializedName("buraq_percentage")
	val buraqPercentage: Int? = null,

	@field:SerializedName("user_id")
	val userId: Int? = null,

	@field:SerializedName("user_detail_id")
	val userDetailId: Int? = null,

	@field:SerializedName("user_driver_detail_service_id")
	val userDriverDetailServiceId: Int? = null
) : Parcelable