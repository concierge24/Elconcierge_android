package com.trava.user.webservices.models.order

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OrganisationCouponUser(
        var organisation_coupon_user_id: Int?,
        var organisation_coupon_id: Int?,
        var customer_user_detail_id: Int?,
        var payment_id: Int?,
        var price: Int?,
        var quantity_left: Int?,
        var expires_at: String?
):Parcelable