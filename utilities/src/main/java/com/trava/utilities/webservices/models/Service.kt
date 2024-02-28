package com.trava.utilities.webservices.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Service(
        var category_id: Int?,
        var category_type: String?,
        var default_brands: String?,
        var name: String?,
        var title: String?,
        var image_url: String?,
        var booking_flow: String?,
        var is_manual_assignment: String?,
        var description: String?,
        var buraq_percentage: Float?,
        var brands: List<Brand> = ArrayList(),
        var image : String?
) : Parcelable