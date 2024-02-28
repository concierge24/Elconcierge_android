package com.codebrew.clikat.modal.other

class PromoCodeModel {
    var status = 0
    var message: String? = null
    var data: DataBean? = null

    class DataBean {
        var discountType = 0
        var discountPrice = 0f
        var promoCode: String? = null
        var max_buy_x_get: String? = null
        var product_ids: String? = null
        var promo_buy_x_quantity: String? = null
        var promo_get_x_quantity: String? = null
        var id = 0
        var minOrder = 0
        var categoryIds: List<Int>? = null
        var supplierIds: List<Int>? = null
        var max_discount_value = 0f
    }
}
