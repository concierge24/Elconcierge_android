package com.trava.user.webservices.models

import com.google.gson.annotations.SerializedName

data class CheckListItem (
    @SerializedName("item_name")
    val item_name: String,
    @SerializedName("price")
    val price: Int,
    @SerializedName("order_id")
    val order_id: Int
    )