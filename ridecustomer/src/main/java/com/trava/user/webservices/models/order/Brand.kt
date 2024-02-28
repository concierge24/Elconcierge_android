package com.trava.user.webservices.models.order

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Brand(
        var category_brand_product_id: Int?,
        var category_id: Int?,
        var category_brand_id: Int?,
        var cancellation_time: Int?,
        var cancellation_charges: Double?,
        var category_sub_type: String?,
        var si_unit: String?,
        var actual_value: Double?,
        var alpha_price: Double?,
        var price_per_quantity: Double?,
        var price_per_distance: Double?,
        var price_per_weight: Double?,
        var sort_order: Int?,
        var multiple: String?,
        var blocked: String?,
        var created_at: String?,
        var updated_at: String?,
        var name: String?,
        var description: String?,
        var brand_name: String?,
        var category_name: String?,
        var product_name: String?
):Parcelable