package com.trava.user.webservices.models.homeapi

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class Product(
        var category_brand_product_id: Int?,
        var name: String?,
        var description: String?,
        var sort_order: Int?,
        var actual_value: Float?,
        var alpha_price: Float?,
        var pool_alpha_price: Float?,
        var pool_price_per_distance: Float?,
        var pool_price_per_hr: Float?,
        var price_per_quantity: Float?,
        var price_per_distance: Float?,
        var price_per_weight: Float?,
        var image_url: String?,
        val icon_image_url: String?,
        val pool: String?,
        val load_unload_charges: Int?,
        val load_unload_time: String?,
        val schedule_charge_type: String?,
        var isSelected: Boolean? = false,
        var min_quantity: Int?,
        var max_quantity: Int,
        var seating_capacity : Int,
        var selected_seating_capacity : Int,
        var price_per_hr : Float,
        var buraq_percentage : Float,
        var schedule_charge : Float,
        var surchargeValue : Int=0,
        var surChargeAdmin : ArrayList<SurChargeData>,
        var driversList: ArrayList<HomeDriver>,

        @field:SerializedName("price_per_min")
        var pricePerMin: Double? = null,

        @field:SerializedName("time_fixed_price")
        val timeFixedPrice: Double? = null,

        @field:SerializedName("distance_price_fixed")
        val distancePriceFixed: Double? = null,

        @field:SerializedName("price_per_km")
        val pricePerKm: Double? = null
):Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readValue(Int::class.java.classLoader) as? Int,
                parcel.readString(),
                parcel.readString(),
                parcel.readValue(Int::class.java.classLoader) as? Int,
                parcel.readValue(Float::class.java.classLoader) as? Float,
                parcel.readValue(Float::class.java.classLoader) as? Float,
                parcel.readValue(Float::class.java.classLoader) as? Float,
                parcel.readValue(Float::class.java.classLoader) as? Float,
                parcel.readValue(Float::class.java.classLoader) as? Float,
                parcel.readValue(Float::class.java.classLoader) as? Float,
                parcel.readValue(Float::class.java.classLoader) as? Float,
                parcel.readValue(Float::class.java.classLoader) as? Float,
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readValue(Int::class.java.classLoader) as? Int,
                parcel.readString(),
                parcel.readString(),
                parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
                parcel.readValue(Int::class.java.classLoader) as? Int,
                parcel.readInt(),
                parcel.readInt(),
                parcel.readInt(),
                parcel.readFloat(),
                parcel.readFloat(),
                parcel.readFloat(),
                parcel.readInt(),
                ArrayList<SurChargeData>(),
                ArrayList<HomeDriver>(),
                parcel.readValue(Double::class.java.classLoader) as? Double,
                parcel.readValue(Double::class.java.classLoader) as? Double,
                parcel.readValue(Double::class.java.classLoader) as? Double,
                parcel.readValue(Double::class.java.classLoader) as? Double) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeValue(category_brand_product_id)
                parcel.writeString(name)
                parcel.writeString(description)
                parcel.writeValue(sort_order)
                parcel.writeValue(actual_value)
                parcel.writeValue(alpha_price)
                parcel.writeValue(pool_alpha_price)
                parcel.writeValue(pool_price_per_distance)
                parcel.writeValue(pool_price_per_hr)
                parcel.writeValue(price_per_quantity)
                parcel.writeValue(price_per_distance)
                parcel.writeValue(price_per_weight)
                parcel.writeString(image_url)
                parcel.writeString(icon_image_url)
                parcel.writeString(pool)
                parcel.writeValue(load_unload_charges)
                parcel.writeString(load_unload_time)
                parcel.writeString(schedule_charge_type)
                parcel.writeValue(isSelected)
                parcel.writeValue(min_quantity)
                parcel.writeInt(max_quantity)
                parcel.writeInt(seating_capacity)
                parcel.writeInt(selected_seating_capacity)
                parcel.writeFloat(price_per_hr)
                parcel.writeFloat(buraq_percentage)
                parcel.writeFloat(schedule_charge)
                parcel.writeInt(surchargeValue)
                parcel.writeValue(pricePerMin)
                parcel.writeValue(timeFixedPrice)
                parcel.writeValue(distancePriceFixed)
                parcel.writeValue(pricePerKm)
        }

        override fun describeContents(): Int {
                return 0
        }

        companion object CREATOR : Parcelable.Creator<Product> {
                override fun createFromParcel(parcel: Parcel): Product {
                        return Product(parcel)
                }

                override fun newArray(size: Int): Array<Product?> {
                        return arrayOfNulls(size)
                }
        }
}

@Parcelize
data class SurChargeData(var category_brand_product_id:Int,var start_time:String,var end_time:String,val value:String,val type:String):Parcelable