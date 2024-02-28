package com.codebrew.clikat.data.model.others

import android.os.Parcelable
import androidx.annotation.Keep
import com.codebrew.clikat.data.RentalDataType
import com.codebrew.clikat.data.model.api.PortData
import kotlinx.android.parcel.Parcelize

@Parcelize
@Keep
data class HomeRentalParam(
    var from_latitude: Double? = null,
    var from_longitude: Double? = null,
    var to_latitude: Double? = null,
    var to_longitude: Double? = null,
    var booking_from_date: String? = null,
    var booking_to_date: String? = null,
    var driveType: Int? = null,
    var from_address: String? = null,
    var startEnd: String? = null,
    var cartId: String? = null,
    var totalAmt: String? = null,
    var to_address: String? = null,
    var netAmount: Double? = null,
    var source_port_id: PortData? = null,
    var destination_port_id: PortData? = null,
    var mRentalType: RentalDataType? = null,
    var mTotalRentalDuration: Int? = null,
    var price: String? = null,
    var handling_admin: Float? = null,
    var product_slot_id: Int? = null,
    var product_slot_price: Float? = null,
    var product_to_time: String? = null,
    var product_from_time: String? = null
) : Parcelable
