package com.trava.user.webservices.models.storybonus

import com.google.gson.annotations.SerializedName

data class StoryBonusPojo(

        @field:SerializedName("msg")
        val msg: String? = null,

        @field:SerializedName("result")
        val result: List<ResultItem?>? = null,

        @field:SerializedName("success")
        val success: Int? = null,

        @field:SerializedName("bonus")
        val bonus: Int? = null,

        @field:SerializedName("statusCode")
        val statusCode: Int? = null
)

data class ResultItem(
        @field:SerializedName("createdAt")
        val createdAt: String? = null,

        @field:SerializedName("user_id")
        val userId: Int? = null,

        @field:SerializedName("credited")
        val credited: Int? = null,

        @field:SerializedName("story_id")
        val storyId: Int? = null,

        @field:SerializedName("id")
        val id: Int? = null,

        @field:SerializedName("updatedAt")
        val updatedAt: String? = null
)
