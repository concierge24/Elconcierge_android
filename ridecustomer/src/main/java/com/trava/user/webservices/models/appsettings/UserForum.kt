package com.trava.user.webservices.models.appsettings

import android.os.Parcelable
import com.trava.utilities.webservices.models.Brand
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserForum(
        var key_name: String?,
        var value: String?,
        var description: String?,
        var terminology: String?,
        var required: String?,
        var optional: String?
) : Parcelable