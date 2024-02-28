package com.trava.user.webservices.models.travelPackages

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.trava.user.webservices.models.etoken.CategoryBrand
import com.trava.user.webservices.models.homeapi.Category
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PricingData(

        @field:SerializedName("category_brands")
        val categoryBrands: ArrayList<CategoryBrandsItem> = ArrayList(),

        @field:SerializedName("categories")
        val categories: ArrayList<Category> = ArrayList()
) : Parcelable