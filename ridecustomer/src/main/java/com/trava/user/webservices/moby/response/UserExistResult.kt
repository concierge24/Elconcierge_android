package com.trava.user.webservices.moby.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.trava.utilities.webservices.models.AppDetail


class UserExistResult{
        @SerializedName("AppDetail")
        @Expose
        private var appDetail: Detail? = null

        fun getAppDetail(): Detail? {
                return appDetail
        }

        fun setAppDetail(appDetail: Detail?) {
                this.appDetail = appDetail
        }
}