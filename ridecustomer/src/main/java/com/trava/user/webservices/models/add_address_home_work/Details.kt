package com.trava.user.webservices.models.add_address_home_work

import com.google.gson.annotations.SerializedName
import com.trava.user.webservices.models.homeapi.User

data class Details(

	@field:SerializedName("vehicle_color")
	val vehicleColor: String? = null,

	@field:SerializedName("mulkiya_front")
	val mulkiyaFront: String? = null,

	@field:SerializedName("mulkiya_back")
	val mulkiyaBack: String? = null,

	@field:SerializedName("timezonez")
	val timezonez: String? = null,

	@field:SerializedName("mulkiya_validity")
	val mulkiyaValidity: Any? = null,

	@field:SerializedName("maximum_rides")
	val maximumRides: Int? = null,

	@field:SerializedName("device_type")
	val deviceType: String? = null,

	@field:SerializedName("language_id")
	val languageId: Int? = null,

	@field:SerializedName("forgot_token")
	val forgotToken: Any? = null,

	@field:SerializedName("organisation_id")
	val organisationId: Int? = null,

	@field:SerializedName("password")
	val password: String? = null,

	@field:SerializedName("mulkiya_number")
	val mulkiyaNumber: String? = null,

	@field:SerializedName("approved")
	val approved: String? = null,

	@field:SerializedName("pan_back")
	val panBack: Any? = null,

	@field:SerializedName("user_detail_id")
	val userDetailId: Int? = null,

	@field:SerializedName("pan_front")
	val panFront: Any? = null,

	@field:SerializedName("vehicle_rc_expiry")
	val vehicleRcExpiry: Any? = null,

	@field:SerializedName("profile_pic_url")
	val profilePicUrl: String? = null,

	@field:SerializedName("longitude")
	val longitude: Double? = null,

	@field:SerializedName("socket_id")
	val socketId: String? = null,

	@field:SerializedName("rc_front")
	val rcFront: Any? = null,

	@field:SerializedName("bearing")
	val bearing: Int? = null,

	@field:SerializedName("otp")
	val otp: String? = null,

	@field:SerializedName("created_by")
	val createdBy: String? = null,

	@field:SerializedName("access_token")
	val accessToken: String? = null,

	@field:SerializedName("government_front")
	val governmentFront: Any? = null,

	@field:SerializedName("user_id")
	val userId: Int? = null,

	@field:SerializedName("forgot_token_validity")
	val forgotTokenValidity: Any? = null,

	@field:SerializedName("accountStep")
	val accountStep: Int? = null,

	@field:SerializedName("notifications")
	val notifications: String? = null,

	@field:SerializedName("vehicle_name")
	val vehicleName: String? = null,

	@field:SerializedName("credit_points")
	val creditPoints: Int? = null,

	@field:SerializedName("breakdown_counter")
	val breakdownCounter: Int? = null,

	@field:SerializedName("Org")
	val org: Any? = null,

	@field:SerializedName("timezone")
	val timezone: String? = null,

	@field:SerializedName("adhaar_front")
	val adhaarFront: Any? = null,

	@field:SerializedName("latitude")
	val latitude: Double? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("category_id")
	val categoryId: Int? = null,

	@field:SerializedName("break_down_charges")
	val breakDownCharges: Int? = null,

	@field:SerializedName("home_address")
	val homeAddress: String? = null,

	@field:SerializedName("other_doc1")
	val otherDoc1: Any? = null,

	@field:SerializedName("blocked")
	val blocked: String? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("vehicle_registration_number")
	val vehicleRegistrationNumber: Any? = null,

	@field:SerializedName("other_doc2")
	val otherDoc2: Any? = null,

	@field:SerializedName("other_doc3")
	val otherDoc3: Any? = null,

	@field:SerializedName("qr_code")
	val qrCode: String? = null,

	@field:SerializedName("user_type_id")
	val userTypeId: Int? = null,

	@field:SerializedName("fcm_id")
	val fcmId: String? = null,

	@field:SerializedName("government_back")
	val governmentBack: Any? = null,

	@field:SerializedName("vehicle_back")
	val vehicleBack: Any? = null,

	@field:SerializedName("category_brand_id")
	val categoryBrandId: Int? = null,

	@field:SerializedName("online_status")
	val onlineStatus: String? = null,

	@field:SerializedName("home_latitude")
	val homeLatitude: Int? = null,

	@field:SerializedName("profile_pic")
	val profilePic: String? = null,

	@field:SerializedName("directional_hire")
	val directionalHire: String? = null,

	@field:SerializedName("vehicle_front")
	val vehicleFront: Any? = null,

	@field:SerializedName("rc_back")
	val rcBack: Any? = null,

	@field:SerializedName("insurance_expiry")
	val insuranceExpiry: Any? = null,

	@field:SerializedName("vehicle_number")
	val vehicleNumber: String? = null,

	@field:SerializedName("home_longitude")
	val homeLongitude: Int? = null,

	@field:SerializedName("device_token")
	val deviceToken: Any? = null,

	@field:SerializedName("government_id_expiry")
	val governmentIdExpiry: Any? = null,

	@field:SerializedName("adhaar_back")
	val adhaarBack: Any? = null,

	@field:SerializedName("user")
	val user: User? = null
)