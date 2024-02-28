package com.trava.user.webservices.models.homeapi

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Category(
    val category_brand_id: Int?= null,
    val sort_order: Int?= null,
    val category_id: Int?= null,
    val name: String?= null,
    val image: String?= null,
    val image_url: String?= null,
    val icon_image_url: String?= null,
    val products: List<Product>?= null,
    var isSelected: Boolean? = false,
    var estimatedTime:String  ?= null,
    var buraq_percentage : Float? = 0.0f,
    var helper_percentage : Float? = 0.0f,
    var driverList : ArrayList<HomeDriver> = ArrayList()
) : Parcelable