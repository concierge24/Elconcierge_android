package com.trava.user.webservices.models.travelPackages

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PackageData(

	@field:SerializedName("image")
	val image: String? = "",

	@field:SerializedName("blocked")
	val blocked: String? = "",

	@field:SerializedName("updated_at")
	val updatedAt: String? = "",

	@field:SerializedName("created_at")
	val createdAt: String? = "",

	@field:SerializedName("distance_kms")
	val distanceKms: Double? = 0.0,

	@field:SerializedName("package_id")
	val packageId: Int? = 0,

	@field:SerializedName("validity")
	val validity: Int? = 0,

	@field:SerializedName("package_type")
	val packageType: String? = "",

	@field:SerializedName("package_pricings")
	val packagePricings: ArrayList<PackagePricingsItem> = ArrayList(),

	@field:SerializedName("pricingData")
	val pricingData : PricingData? = null ,

	@field:SerializedName("sort_order")
	val sortOrder: Int? = 0
) : Parcelable