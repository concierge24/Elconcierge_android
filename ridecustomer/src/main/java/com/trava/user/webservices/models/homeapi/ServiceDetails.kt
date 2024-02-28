package com.trava.user.webservices.models.homeapi

import com.trava.user.webservices.models.Addresses
import com.trava.user.webservices.models.Versioning
import com.trava.user.webservices.models.order.Order

data class ServiceDetails(
        val categories: List<Category>?,
        val drivers: List<HomeDriver>?,
        val Details: Details?,
        val payment_body: String?,
        val addresses: Addresses? = null,
        val currentOrders: List<Order>?,
        val lastCompletedOrders: List<Order>?,
        val Versioning: Versioning?
)

data class TerminologyData(val categoryData: categoryData, val key_value: key_value)
data class categoryData(var category_id: Int, val text: String, val blocked: String, val elevator: String, val fragile: String)
data class key_value(val halfway_ride_stop: String, val breakdown_stop: String, val panic_button: String,val check_list:Int)