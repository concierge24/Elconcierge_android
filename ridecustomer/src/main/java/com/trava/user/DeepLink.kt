package com.trava.user

object DeepLink {
    const val BOOK_RIDE = "/bookride"
    const val OPEN_BOOKINGS = "/bookings"
    const val OPEN_ROYO_RIDES = "/open"

    /**
     * Parameter types for the deep-links
     */
    object Params {
        const val ACTIVITY_TYPE = "destination"
    }

    object Actions {
        const val ACTION_TOKEN_EXTRA = "actions.fulfillment.extra.ACTION_TOKEN"
    }
}