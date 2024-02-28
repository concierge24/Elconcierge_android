package com.trava.user.webservices.models

import java.io.Serializable

class CardModel(
        var user_card_id: String? = null,
        var card_brand: String? = null,
        var card_no: String? = null,
        var exp_month: String? = null,
        var exp_year: String? = null,
        var card_holder_name: String? = null,
        var card_number: String? = null,
        var customer_token: String? = null,
        var last4: String? = null,
        var card_id: String? = null,
        var isSelected: Boolean? = null,
        var deleted: Boolean? = null
) : Serializable