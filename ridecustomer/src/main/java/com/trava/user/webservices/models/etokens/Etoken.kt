package com.trava.user.webservices.models.etokens

data class Etoken(
        var organisation_coupon_id: Int?,
        var organisation_id: Int?,
        var category_brand_product_id: Int?,
        var price: Int?,
        var description: String?,
        var quantity: Int?,
        var expiry_days: Int?,
        var category_brand_product_name: String?
)