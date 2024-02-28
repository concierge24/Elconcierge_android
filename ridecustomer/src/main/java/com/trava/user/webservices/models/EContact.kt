package com.trava.user.webservices.models

import com.google.gson.annotations.SerializedName

data class EContact(

	@field:SerializedName("emergency_contact_id")
	val emergencyContactId: Int? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("image")
	val image: String? = null,

	@field:SerializedName("phone_number")
	val phoneNumber: Long? = null,

	@field:SerializedName("sort_order")
	val sortOrder: Int? = null
)