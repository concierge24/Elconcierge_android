package com.trava.user.webservices.models

import com.google.gson.annotations.SerializedName


data class SendMoneyResponseModel (

		@SerializedName("wallet_log_id") val wallet_log_id : String? = null,
		@SerializedName("user_wallet_id") val user_wallet_id : String? = null,
		@SerializedName("sender_user_detail_id") val sender_user_detail_id : String? = null,
		@SerializedName("sender_user_id") val sender_user_id : String? = null,
		@SerializedName("receiver_user_detail_id") val receiver_user_detail_id : String? = null,
		@SerializedName("receiver_user_id") val receiver_user_id : String? = null,
		@SerializedName("amount") val amount : String? = null,
		@SerializedName("transaction_type") val transaction_type : String? = null,
		@SerializedName("status") val status : String? = null,
		@SerializedName("transaction_by") val transaction_by : String? = null
)