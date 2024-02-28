package com.trava.user.webservices.models.roadPickup

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RoadPickupResponse(

	@field:SerializedName("vehicle_color")
	val vehicleColor: String? = null,

	@field:SerializedName("user_driver_detail_products")
	val userDriverDetailProducts: List<UserDriverDetailProductsItem?>? = null,

	@field:SerializedName("adhaar_front")
	val adhaarFront: String? = null,

	@field:SerializedName("latitude")
	val latitude: Double? = null,

	@field:SerializedName("mulkiya_front")
	val mulkiyaFront: String? = null,

	@field:SerializedName("mulkiya_back")
	val mulkiyaBack: String? = null,

	@field:SerializedName("mulkiya_validity")
	val mulkiyaValidity: String? = null,

	@field:SerializedName("language_id")
	val languageId: Int? = null,

	@field:SerializedName("mulkiya_number")
	val mulkiyaNumber: String? = null,

	@field:SerializedName("category_id")
	val categoryId: Int? = null,

	@field:SerializedName("pan_back")
	val panBack: String? = null,

	@field:SerializedName("other_doc1")
	val otherDoc1: String? = null,

	@field:SerializedName("vehicle_registration_number")
	val vehicleRegistrationNumber: String? = null,

	@field:SerializedName("other_doc2")
	val otherDoc2: String? = null,

	@field:SerializedName("other_doc3")
	val otherDoc3: String? = null,

	@field:SerializedName("user_detail_id")
	val userDetailId: Int? = null,

	@field:SerializedName("pan_front")
	val panFront: String? = null,

	@field:SerializedName("qr_code")
	val qrCode: String? = null,

	@field:SerializedName("user_type_id")
	val userTypeId: Int? = null,

	@field:SerializedName("user_driver_detail_services")
	val userDriverDetailServices: List<UserDriverDetailServicesItem?>? = null,

	@field:SerializedName("longitude")
	val longitude: Double? = null,

	@field:SerializedName("government_back")
	val governmentBack: String? = null,

	@field:SerializedName("vehicle_back")
	val vehicleBack: String? = null,

	@field:SerializedName("category_brand_id")
	val categoryBrandId: Int? = null,

	@field:SerializedName("rc_front")
	val rcFront: String? = null,

	@field:SerializedName("profile_pic")
	val profilePic: String? = null,

	@field:SerializedName("rc_back")
	val rcBack: String? = null,

	@field:SerializedName("vehicle_front")
	val vehicleFront: String? = null,

	@field:SerializedName("access_token")
	val accessToken: String? = null,

	@field:SerializedName("government_front")
	val governmentFront: String? = null,

	@field:SerializedName("user_id")
	val userId: Int? = null,

	@field:SerializedName("vehicle_number")
	val vehicleNumber: String? = null,

	@field:SerializedName("accountStep")
	val accountStep: Int? = null,

	@field:SerializedName("adhaar_back")
	val adhaarBack: String? = null,

	@field:SerializedName("user")
	val user: User? = null,

	@field:SerializedName("vehicle_name")
	val vehicleName: String? = null
) : Parcelable