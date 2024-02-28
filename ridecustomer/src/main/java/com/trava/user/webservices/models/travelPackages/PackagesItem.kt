package com.trava.user.webservices.models.travelPackages

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PackagesItem(

        @field:SerializedName("package")
        val packageData: PackageData? = null,

        @field:SerializedName("terms")
        val terms: String? = "",

        @field:SerializedName("name")
        val name: String? = "",

        @field:SerializedName("description")
        val description: String? = "",

        @field:SerializedName("package_id")
        val packageId: Int? = 0
) : Parcelable