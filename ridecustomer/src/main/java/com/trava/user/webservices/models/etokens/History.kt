package com.trava.user.webservices.models.etokens

data class History(
        var organisation_coupon_user_id: Int?,
        var organisation_coupon_id: Int?,
        var category_id: Int?,
        var category_brand_id: Int?,
        var category_brand_product_id: Int?,
        var seller_user_id: Int?,
        var seller_user_detail_id: Int?,
        var seller_user_type_id: Int?,
        var seller_organisation_id: Int?,
        var customer_user_id: Int?,
        var customer_user_detail_id: Int?,
        var customer_user_type_id: Int?,
        var customer_organisation_id: Int?,
        var payment_id: Int?,
        var price: Int?,
        var quantity: Int?,
        var quantity_left: Int?,
        var expiry_days: Int?,
        var created_at: String?,
        var updated_at: String?,
        var expires_at: String?,
        var brand: Brand?,
        var isSelected :Boolean= false
)