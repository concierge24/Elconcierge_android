package com.trava.user.webservices.models.homeapi

data class User(
        var user_id: Int,
        var organisation_id: Int,
        var stripe_customer_id: String,
        var stripe_connect_id: String,
        var name: String,
        var email: String,
        var phone_code: String,
        var phone_number: Long,
        var stripe_connect_token: String,
        var blocked: String,
        var created_by: String,
        var created_at: String,
        var updated_at: String
)