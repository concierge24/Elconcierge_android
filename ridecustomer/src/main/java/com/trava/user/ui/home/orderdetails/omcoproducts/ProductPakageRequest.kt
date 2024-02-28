package com.trava.user.ui.home.orderdetails.omcoproducts

import com.trava.user.webservices.models.ProductListModel
import org.json.JSONArray

data class ProductPakageRequest(var pickup_address: String, var pickup_latitude: String, var pickup_longitude: String,
                                var receiver_address_id: String, var dropoff_address: String,
                                var dropoff_latitude: String,
                                var dropoff_longitude: String, var product_detail: ArrayList<ProductListModel>,
                                var category_id: String, var category_brand_id: String,
                                var category_brand_product_id: String, var booking_type: String, var order_type: String)

