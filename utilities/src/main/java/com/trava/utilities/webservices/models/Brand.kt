package com.trava.utilities.webservices.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Brand(
        var category_brand_id: Int?,
        var sort_order: Int?,
        var category_id: Int?,
        var name: String?,
        var image: String?,
        var image_url: String?
) : Parcelable{
    override fun toString(): String {
        return name?:""
    }
}