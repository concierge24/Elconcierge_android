package com.trava.user.webservices.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StopsModel(
        var latitude : Double?,
        var longitude : Double?,
        var priority : Int,
        var address : String,
        var stop_status : String,
        var ride_stop_id : String,
        var added_with_ride : String
):Parcelable