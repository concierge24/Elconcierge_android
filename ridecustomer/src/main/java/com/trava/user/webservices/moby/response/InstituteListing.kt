package com.trava.user.webservices.moby.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class InstituteListing {
    @SerializedName("name")
    @Expose
    private var name: String? = null
    @SerializedName("cooperation_id")
    @Expose
    private var cooperationId: Int? = null
    @SerializedName("type")
    @Expose
    private var type: String? = null

    fun getName(): String? {
        return name
    }

    fun setName(name: String?) {
        this.name = name
    }

    fun getCooperationId(): Int? {
        return cooperationId
    }

    fun setCooperationId(cooperationId: Int?) {
        this.cooperationId = cooperationId
    }

    fun getType(): String? {
        return type
    }

    fun setType(type: String?) {
        this.type = type
    }

}