package com.trava.user.webservices.models.etoken

data class Etokenn(
        var organisation_coupon_id: Int?,
        var organisation_id: Int?,
        var category_id: Int?,
        var category_brand_id: Int?,
        var category_brand_product_id: Int?,
        var price: Float?,
        var quantity: Int?,
        var description: String?,
        var categoryBrand: CategoryBrand?,
        var categoryBrandProduct: CategoryBrandProduct?
)