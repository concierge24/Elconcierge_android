package com.trava.user.webservices.models.order

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Driver(
        var phone_number: String?,
        var user_id: Int?,
        var name: String?,
        var user_detail_id: Int?,
        var user_type_id: Int?,
        var category_id: Int?,
        var category_brand_id: Int?,
        var profile_pic: String?,
        var phone_code: String?,
        var latitude: Double?,
        var longitude: Double?,
        var profile_pic_url: String?,
        var rating_count: Int?,
        var rating_avg: String,
        var vehicle_number : String,
        var vehicle_name : String,
        var vehicle_color : String,
        var vehicle_model : String,
        var vehicle_purchase_year : String,
        var vehicle_brand : String,
        var icon_image_url : String,
        var vehicle_front : String
):Parcelable