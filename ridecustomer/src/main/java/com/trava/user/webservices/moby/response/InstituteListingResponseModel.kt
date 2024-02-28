package com.trava.user.webservices.moby.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class InstituteListingResponseModel{
    @SerializedName("success")
    @Expose
    private val success: Int? = null
    @SerializedName("statusCode")
    @Expose
    private val statusCode: Int? = null
    @SerializedName("result")
    @Expose
    private val result: List<InstituteListing>? = null

}
