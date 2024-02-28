package com.trava.user.webservices.models.homeapi

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


data class HomeDriver(
        var user_detail_id: Int?,
        var user_type_id: Int?,
        var category_id: Int?,
        var category_brand_id: Int?,
        var category_brand_product_id: Int?,
        var organisation_id: Int?,
        var latitude: Double?,
        var longitude: Double?,
        var bearing: Float?,
        var distance: Double?,
        var name: String?,
        var profile_pic: String?,
        var vehicle_number: String?,
        var o_blocked: String?,
        var o_serving: Int?,
        var icon_image_url : String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readValue(Double::class.java.classLoader) as? Double,
            parcel.readValue(Double::class.java.classLoader) as? Double,
            parcel.readValue(Float::class.java.classLoader) as? Float,
            parcel.readValue(Double::class.java.classLoader) as? Double,
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(user_detail_id)
        parcel.writeValue(user_type_id)
        parcel.writeValue(category_id)
        parcel.writeValue(category_brand_id)
        parcel.writeValue(category_brand_product_id)
        parcel.writeValue(organisation_id)
        parcel.writeValue(latitude)
        parcel.writeValue(longitude)
        parcel.writeValue(bearing)
        parcel.writeValue(distance)
        parcel.writeString(name)
        parcel.writeString(profile_pic)
        parcel.writeString(vehicle_number)
        parcel.writeString(o_blocked)
        parcel.writeValue(o_serving)
        parcel.writeString(icon_image_url)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HomeDriver> {
        override fun createFromParcel(parcel: Parcel): HomeDriver {
            return HomeDriver(parcel)
        }

        override fun newArray(size: Int): Array<HomeDriver?> {
            return arrayOfNulls(size)
        }
    }

}