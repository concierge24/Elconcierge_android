package com.trava.user.webservices.models.orderrequest

import com.google.gson.annotations.SerializedName

data class OrderRequestResponse(

	@field:SerializedName("msg")
	val msg: String? = null,

	@field:SerializedName("result")
	val result: ArrayList<ResultItem?>? = null,

	@field:SerializedName("success")
	val success: Int? = null,

	@field:SerializedName("statusCode")
	val statusCode: Int? = null
)

data class ResultItem(

	@field:SerializedName("pickup_longitude")
	val pickupLongitude: String? = null,

	@field:SerializedName("product_image")
	val productImage: Any? = null,

	@field:SerializedName("order_number")
	val orderNumber: Int? = null,

	@field:SerializedName("pickup_address")
	val pickupAddress: String? = null,

	@field:SerializedName("receiver_address_id")
	val receiverAddressId: String? = null,

	@field:SerializedName("sender_user_detail_id")
	val senderUserDetailId: Int? = null,

	@field:SerializedName("dropoff_address")
	val dropoffAddress: String? = null,

	@field:SerializedName("dropoff_latitude")
	val dropoffLatitude: String? = null,

	@field:SerializedName("product_code")
	val productCode: String? = null,

	@field:SerializedName("product_name")
	val productName: String? = null,

	@field:SerializedName("receiver_phone_number")
	val receiverPhoneNumber: String? = null,

	@field:SerializedName("order_status")
	val orderStatus: String? = null,

	@field:SerializedName("createdAt")
	val createdAt: String? = null,

	@field:SerializedName("dropoff_longitude")
	val dropoffLongitude: String? = null,

	@field:SerializedName("pickup_latitude")
	val pickupLatitude: String? = null,

	@field:SerializedName("receiver_name")
	val receiverName: String? = null,

	@field:SerializedName("order_id")
	val orderId: Int? = null,

	@field:SerializedName("sender_user_id")
	val senderUserId: Int? = null,

	@field:SerializedName("receiver_phone_code")
	val receiverPhoneCode: String? = null,

	@field:SerializedName("updatedAt")
	val updatedAt: String? = null
)
