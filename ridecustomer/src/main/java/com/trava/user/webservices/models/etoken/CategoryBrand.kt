package com.trava.user.webservices.models.etoken

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CategoryBrand(
        var category_brand_detail_id: Int?,
        var category_brand_id: Int?,
        var name: String?,
        var image: String?,
        var image_url: String?,
        var description: String?
) : Parcelable