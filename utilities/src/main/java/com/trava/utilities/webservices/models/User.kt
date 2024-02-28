package com.trava.utilities.webservices.models

data class User(
        var user_id: Int?=null,
        var organisation_id: Int?=null,
        var stripe_customer_id: String?=null,
        var stripe_connect_id: String?=null,
        var name: String?=null,
        var firstName: String?=null,
        var lastName: String?=null,
        var gender: String?=null,
        var email: String?=null,
        var phone_code: String?=null,
        var profile_pic_url: String?=null,
        var rating_avg: String?=null,
        var rating_count: Int?=null,
        var phone_number: Long?=null,
        var stripe_connect_token: String?=null,
        val referral_code: String?=null

)