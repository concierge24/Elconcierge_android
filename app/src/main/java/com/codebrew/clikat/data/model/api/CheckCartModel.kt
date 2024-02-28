package com.codebrew.clikat.data.model.api

import android.os.Parcelable
import com.codebrew.clikat.modal.other.ProductDataBean
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class CheckCartModel(
        val data: CartData? = null,
        val message: String? = null,
        val status: Int? = null
)

data class CartData(
        val result: List<ProductDataBean>? = null,
        var tips: ArrayList<Int>? = null,
        val is_dine_in: Int? = null,
        var min_order: Float? = null,
        var base_delivery_charges: Float? = null,
        val not_available_ids: List<String>,
        val payment_gateways: ArrayList<String>,
        val referralAmount: Int,
        val userSubscriptionData: SubcripModel,
        val region_delivery_charge: Float,
        val loyalitPointDiscountAmount: Float,
        val loyalityLevelDiscountAmount: Float,
        val loyalty_points:Float?=null,
        var min_order_distance_wise: ArrayList<MinimumOrderDistance>,
        var supplier_weight_wise_delivery_charge: ArrayList<WeightWiseData>,
        var supplier_delivery_types: ArrayList<SupplierDeliveryTypes>,
        var order_type_wise_gateways: ArrayList<SupplierDeliveryTypes>,
        var suppliers_delivery_companies: ArrayList<SuppliersDeliveryCompaniesItem>

)

@Parcelize
class MinimumOrderDistance(
        var distance: Float? = null,
        var supplier_id: String? = null,
        var min_amount: Float? = null,
) : Parcelable, Comparable<MinimumOrderDistance> {
    override fun compareTo(other: MinimumOrderDistance): Int {
        return distance?.compareTo(other.distance ?: 0f) ?: 0
    }
}

@Parcelize
class WeightWiseData(
        var id: String? = null,
        var supplier_id: String? = null,
        var weight: Float? = null,
        var delivery_charge: Float? = null
) : Parcelable, Comparable<WeightWiseData> {
    override fun compareTo(other: WeightWiseData): Int {
        return weight?.compareTo(other.weight ?: 0f) ?: 0
    }
}

@Parcelize
class SupplierDeliveryTypes(
        var id: String? = null,
        var supplier_id: String? = null,
        var type: Int? = null,
        var buffer_time: Int? = null,
        var order_type: Int? = null,
        var payment_gateways: String? = null,
        val price: Float? = null) : Parcelable

@Parcelize
data class SuppliersDeliveryCompaniesItem(

        @field:SerializedName("base_delivery_charges")
        val baseDeliveryCharges: Float? = null,

        @field:SerializedName("is_block")
        val isBlock: Int? = null,

        @field:SerializedName("iso")
        val iso: String? = null,

        @field:SerializedName("logo_url")
        val logoUrl: String? = null,

        @field:SerializedName("latitude")
        val latitude: Double? = null,

        @field:SerializedName("created_at")
        val createdAt: String? = null,

        @field:SerializedName("radius_price")
        val radiusPrice: Float? = null,

        @field:SerializedName("password")
        val password: String? = null,

        @field:SerializedName("is_deleted")
        val isDeleted: Int? = null,

        @field:SerializedName("updated_at")
        val updatedAt: String? = null,

        @field:SerializedName("delivery_min_time")
        val deliveryMinTime: Int? = null,

        @field:SerializedName("id")
        val id: Int? = null,

        @field:SerializedName("distance_value")
        val distanceValue: Float? = null,

        @field:SerializedName("email")
        val email: String? = null,

        @field:SerializedName("delivery_radius")
        val deliveryRadius: Float? = null,

        @field:SerializedName("longitude")
        val longitude: Double? = null,

        @field:SerializedName("address")
        val address: String? = null,

        @field:SerializedName("image_url")
        val imageUrl: String? = null,

        @field:SerializedName("is_verified")
        val isVerified: Int? = null,

        @field:SerializedName("access_token")
        val accessToken: String? = null,

        @field:SerializedName("country_code")
        val countryCode: String? = null,

        @field:SerializedName("device_token")
        val deviceToken: String? = null,

        @field:SerializedName("name")
        val name: String? = null,

        @field:SerializedName("phone_number")
        val phoneNumber: String? = null,

        @field:SerializedName("supplier_id")
        val supplierId: Int? = null,

        @field:SerializedName("status")
        val status: Int? = null
):Parcelable{
    override fun toString(): String {
        return name?:""
    }
}
