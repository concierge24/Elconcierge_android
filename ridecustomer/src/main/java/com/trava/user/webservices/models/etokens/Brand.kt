package com.trava.user.webservices.models.etokens

data class Brand(
        var category_brand_id: Int?,
        var category_id: Int?,
        var category_brand_name: String?,
        var image: String?,
        var image_url: String?,
        var description: String?,
        var etokens_count: Int?,
        var etokens: List<Etoken>?
)