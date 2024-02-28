package com.trava.driver.webservices.models

import java.io.Serializable

data class WalletBalModel(
        var amount: String? = null,
        var recharge_amt: String? = null,
        var lowBalanceAlert: Boolean? = null,
        var details: WalletDetails? = null
) : Serializable

data class WalletDetails(
        var insured: Int? = null,
        var minBalance: Float? = null,
        var approved : String ?= null,
        var lowBalanceAlert: Boolean? = null
) : Serializable