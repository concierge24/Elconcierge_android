package com.trava.driver.webservices.models

import java.io.Serializable

data class WalletHisResponse(
        var success: Int? = null,
        var statusCode: Int? = null,
        var msg: String? = null,
        var result: ArrayList<WalletModel>? = null,
        var amount: String? = null,
        var balance : String ?= null

) : Serializable

data class WalletModel(
        var wallet_recharge_id: String? = null,
        var user_detail_id: String? = null,
        var amount: String? = null,
        var recharge_txn_id: String? = null,
        var recharge_by: String? = null,
        var created_at :String ?= null,
        var recharge_amt : String ?= null,
        var comment : String ?= null,
        var updated_balance : String ?= null

) : Serializable


