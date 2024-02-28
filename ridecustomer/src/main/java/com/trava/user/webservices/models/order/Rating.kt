package com.trava.user.webservices.models.order

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Rating(
        var order_rating_id: Int,
        var ratings: Int,
        var comments: String,
        var created_by: String,
        var created_at: String
):Parcelable