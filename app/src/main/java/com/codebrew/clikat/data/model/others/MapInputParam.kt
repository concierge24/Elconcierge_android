package com.codebrew.clikat.data.model.others

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MapInputParam
(val latitude: String, val longitude: String, val first_address: String, val second_address: String, val requestType: String, val addressId: String,
 var name: String? = null,var reference_address:String?=null, var phone_number: String? = null, var country_code:String?=null)
    : Parcelable