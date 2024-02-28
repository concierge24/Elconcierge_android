package com.trava.user.webservices.models

import com.trava.user.webservices.models.stories.ResultItem
import java.math.BigInteger

data class TrackingModel(
        var type: String?,
        var order_id: BigInteger?,
        var driver_id: Int?,
        var latitude: Double?,
        var longitude: Double?,
        var order_status: String?,
        var bearing: Double?,
        var my_turn: String?,
        var polyline: PolylineModel?,
        var updated_at : String?,
        var accepted_at : String?,
        val stories: List<ResultItem?>?
)