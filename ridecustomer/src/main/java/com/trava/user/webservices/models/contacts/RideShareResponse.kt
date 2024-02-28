package com.trava.user.webservices.models.contacts

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.trava.user.webservices.models.cards_model.UserCardData
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RideShareResponse (
    @field:SerializedName("order_id")
    var orderId: String? = "",

    @field:SerializedName("shareWith")
    var shareWith: ArrayList<ContactModel> = ArrayList(),

    @field:SerializedName("credit_points")
    var creditPoints: Int? = 0,

    @field:SerializedName("cards")
    var cards: ArrayList<UserCardData> = ArrayList(),

    @field:SerializedName("url")
    var url: String? = ""

): Parcelable