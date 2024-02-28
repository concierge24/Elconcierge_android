package com.trava.user.webservices.models.order

import android.os.Parcelable
import com.trava.user.webservices.models.StopsModel
import com.trava.user.webservices.models.contacts.ContactModel
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.math.BigInteger
@Parcelize
data class Order(
        var order_id: BigInteger?,
        var booking_type: String?,
        var order_token: String?,
        var delivery_proof1: String?,
        var delivery_proof2: String?,
        var exact_path: String?,
        var customer_signature: String?,
        var customer_user_id: Int?,
        var customer_user_detail_id: Int?,
        var customer_organisation_id: Int?,
        var customer_user_type_id: Int?,
        var driver_user_id: Int?,
        var driver_user_detail_id: Int?,
        var driver_organisation_id: Int?,
        var driver_user_type_id: Int?,
        var category_id: Int?,
        var package_id: Int?,
        var category_brand_id: Int?,
        var category_brand_product_id: Int?,
        var continuous_order_id: Int?,
        var order_status: String?,
        var otp: String?,
        var payment_type: String?,
        var material_details: String?,
        var product_weight: String?,
        var details: String?,
        var pickup_person_name: String?,
        var pickup_person_phone: String?,
        var invoice_number: String?,
        var delivery_person_name: String?,
        var booking_flow: String?,
        var pickup_address: String?,
        var pickup_latitude: Double?,
        var pickup_longitude: Double?,
        var dropoff_address: String?,
        var dropoff_latitude: Double?,
        var dropoff_longitude: Double?,
        var order_timings: String?,
        var future: String?,
        var cancel_reason: String?,
        var cancelled_by: String?,
        var organisation_coupon_user_id: Int?,
        var coupon_user_id: Int?,
        var numberOfRiders: Int?,
        var half_way_status: String?,
        var waiting_time: String?,
        var track_path: String?,
        var track_image: String?,
        var payment: Payment?,
        var organisation_coupon_user: OrganisationCouponUser?,
        var brand: Brand?,
        var driver: Driver?,
        var cRequest: Timings?,
        var ratingByUser: Rating?,
        var ratingByDriver: Rating?,
        var helper: @RawValue Any?,
        var order_helper: order_helper?,
        val my_turn: String?,
        val shareWith: ArrayList<ContactModel> = ArrayList(),
        val updated_at: String?,
        var accepted_at: String?,
        var payment_id: Int?,
        var cancellation_charges: Double?,
        var check_list_total: Double?,
        var ride_stops: ArrayList<StopsModel> = ArrayList(),
        var onGoing_ride_stops: ArrayList<StopsModel> = ArrayList(),
        var order_images_url: ArrayList<OrderImageUrls> = ArrayList(),
        var check_lists: ArrayList<CheckListModelArray> = ArrayList(),
        var amount: String?,
        var km_bonus: String?,
        var coupon_detail: CouponDetail?
) : Parcelable

@Parcelize
data class OrderImageUrls(
        var order_id: Long?,
        var image: String?,
        var image_url: String?
) : Parcelable

@Parcelize
data class CheckListModelArray(var check_list_id: Int, val order_id: Int, val item_name: String, var after_item_price: String,
                               val tax: Int, var updated_by: String) : Parcelable

@Parcelize
data class helper(var profile_pic_url: String
                  , var firstName: String?, var lastName: String,
                  var phone_code: String, var phone_number: String) : Parcelable

@Parcelize
data class order_helper(var otp_entered: String, var otp: String?) : Parcelable

