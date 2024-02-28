package com.trava.user.webservices.models.noticeBoard

import com.google.gson.annotations.SerializedName

data class NotificationData(

	@field:SerializedName("user_detail_notification_id")
	val userDetailNotificationId: Int? = null,

	@field:SerializedName("order_rating_id")
	val orderRatingId: Int? = null,

	@field:SerializedName("order_request_id")
	val orderRequestId: Int? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("user_organisation_id")
	val userOrganisationId: Int? = null,

	@field:SerializedName("sender_user_detail_id")
	val senderUserDetailId: Int? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("is_read")
	val isRead: String? = null,

	@field:SerializedName("sender_type_id")
	val senderTypeId: Int? = null,

	@field:SerializedName("organisation_id")
	val organisationId: Any? = null,

	@field:SerializedName("deleted")
	val deleted: String? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("user_id")
	val userId: Int? = null,

	@field:SerializedName("user_detail_id")
	val userDetailId: Int? = null,

	@field:SerializedName("user_type_id")
	val userTypeId: Int? = null,

	@field:SerializedName("order_id")
	val orderId: Int? = null,

	@field:SerializedName("sender_user_id")
	val senderUserId: Int? = null,

	@field:SerializedName("sender_organisation_id")
	val senderOrganisationId: Int? = null
)