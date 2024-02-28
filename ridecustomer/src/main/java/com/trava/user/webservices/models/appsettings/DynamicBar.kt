package com.trava.user.webservices.models.appsettings

data class DynamicBar(
        val my_bookings : Boolean,
        val app_notifications : Boolean,
        val app_settings : Boolean,
        val is_wallet : Boolean,
        val is_Refer_and_Earn : Boolean,
        val heat_maps : Boolean,
        val contact_us : Boolean,
        val travel_packages : Boolean,
        val road_pickup : Boolean,
        val emergency_contacts : Boolean
)
