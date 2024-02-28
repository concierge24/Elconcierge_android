package com.trava.user.webservices.models.etoken

data class Org(
        var organisation_id: Int,
        var name: String,
        var image: String,
        var image_url: String,
        var buraq_percentage: Int,
        var bottle_charge: String
)