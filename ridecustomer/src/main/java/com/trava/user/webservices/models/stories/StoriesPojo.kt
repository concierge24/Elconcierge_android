package com.trava.user.webservices.models.stories

import com.google.gson.annotations.SerializedName

data class StoriesPojo<T>(
        @field:SerializedName("msg")
        val msg: String? = null,

        @field:SerializedName("result")
        val result: List<ResultItem?>? = null,

        @field:SerializedName("success")
        val success: Int? = null,

        @field:SerializedName("statusCode")
        val statusCode: Int? = null
)

data class ResultItem(
        @field:SerializedName("address_latitude")
        val addressLatitude: Double? = null,

        @field:SerializedName("thumbnail")
        val thumbnail: String? = null,
        @field:SerializedName("description")
        val description: String? = null,
        @field:SerializedName("name")
        val name: String? = null,

        @field:SerializedName("address")
        val address: Any? = null,

        @field:SerializedName("advertised_to")
        val advertisedTo: String? = null,

        @field:SerializedName("distance")
        val distance: Double? = null,

        @field:SerializedName("image_url")
        val imageUrl: String? = null,

        @field:SerializedName("type")
        val type: String? = null,

        @field:SerializedName("duration")
        val duration: Int? = null,

        @field:SerializedName("advertised_from")
        val advertisedFrom: String? = null,

        @field:SerializedName("video_url")
        val videoUrl: String? = null,

        @field:SerializedName("category_id")
        val categoryId: Int? = null,

        @field:SerializedName("website_url")
        val websiteUrl: String? = null,

        @field:SerializedName("address_longitude")
        val addressLongitude: Double? = null,

        @field:SerializedName("id")
        val id: Int? = null,
        var tempConunt: Int? = 0
)
