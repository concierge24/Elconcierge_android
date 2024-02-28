package com.trava.user.webservices.models.order

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Timings(
        var accepted_at: String?,
        var confirmed_at: String?,
        var started_at: String?,
        var updated_at: String?
):Parcelable