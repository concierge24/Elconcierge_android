package com.trava.user.webservices.models.applabels

data class AppLabels<T>(
    val data: Data,
    val msg: String,
    val statusCode: Int,
    val success: Int
)