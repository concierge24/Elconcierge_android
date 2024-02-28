package com.codebrew.clikat.data.model.api

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class PortModel(
        val data: List<PortData>,
        val message: String,
        val status: Int
)
@Parcelize
data class PortData(
        val address: String?=null,
        val id: Int?=null,
        val name: String?=null
):Parcelable