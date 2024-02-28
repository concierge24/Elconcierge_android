package com.trava.user.webservices.models.etoken

data class PurchasedEToken(
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
        var bottle_quantity: Int?,
        var quantity: Int?,
        var quantity_left: Int?,
        var address: String?,
        var address_latitude: Double?,
        var address_longitude: Double?,
        var created_at: String?,
        var updated_at: String?,
        var pendingPaymentAmount: Double?,
        var paidPaymentAmount: Double?,
        var sellerOrg: SellerOrg?,
        var categoryBrandProduct: CategoryBrandProduct?,
        var categoryBrand : CategoryBrand?
)