package com.trava.user.webservices.models

import com.google.gson.annotations.SerializedName

data class Msg (
    @SerializedName("result") val result : List<CheckListItem>
)