package com.trava.user.webservices.models.eContacts

import com.google.gson.annotations.SerializedName


data class EContactsListing (

        @SerializedName("emergency_contact_id") val emergency_contact_id : Int,
        @SerializedName("sort_order") val sort_order : String,
        @SerializedName("phone_number") val phone_number : String,
        @SerializedName("name") val name : String,
        @SerializedName("phone_code") val phone_code : String,
        @SerializedName("added_by") val added_by : String,
        @SerializedName("user_id") val user_id : Int,
        @SerializedName("blocked") val blocked : String,
        @SerializedName("deleted") val deleted : Int,
        @SerializedName("created_at") val created_at : String,
        @SerializedName("updated_at") val updated_at : String
)