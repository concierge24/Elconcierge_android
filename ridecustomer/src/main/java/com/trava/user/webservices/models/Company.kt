package com.trava.user.webservices.models

data class Company(
        var organisation_id: Int,
        var name: String,
        var image: String,
        var image_url: String,
        var phone_code: String,
        var phone_number: String,
        var sort_order: Int,
        var sunday_service: String,
        var monday_service: String,
        var tuesday_service: String,
        var wednesday_service: String,
        var thursday_service: String,
        var friday_service: String,
        var saturday_service: String
)