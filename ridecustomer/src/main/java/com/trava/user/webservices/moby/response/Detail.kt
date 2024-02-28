package com.trava.user.webservices.moby.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Detail{
    @SerializedName("userExists")
    @Expose
    private var userExists: Boolean? = null

    fun getUserExists(): Boolean? {
        return userExists
    }

    fun setUserExists(userExists: Boolean?) {
        this.userExists = userExists
    }
}