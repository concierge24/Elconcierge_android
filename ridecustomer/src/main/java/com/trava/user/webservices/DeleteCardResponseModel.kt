package com.trava.user.webservices

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class DeleteCardResponseModel(@SerializedName("result")
                                   @Expose
                                   var result: Int? = null)


