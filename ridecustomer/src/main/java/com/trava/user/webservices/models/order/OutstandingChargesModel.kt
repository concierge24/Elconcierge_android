package com.trava.user.webservices.models.order

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OutstandingChargesModel (
        var payment_id: Int?,
        var cancellation_charges: Double?,
        var paymentType: String?,
        var updated_at: String?
) : Parcelable