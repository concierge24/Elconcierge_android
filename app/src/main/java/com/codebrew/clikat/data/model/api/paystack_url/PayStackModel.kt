package com.codebrew.clikat.data.model.api.paystack_url

data class PayStackModel(
    val data: PayStackData,
    val message: String,
    val status: Int
)

data class PayStackData(
        val data: PayStackCode,
        val message: String,
        val status: Boolean
)

data class PayStackCode(
        val access_code: String,
        val authorization_url: String,
        val reference: String
)