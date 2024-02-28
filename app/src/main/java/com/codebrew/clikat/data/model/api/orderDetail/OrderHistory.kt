package com.codebrew.clikat.data.model.api.orderDetail

import android.os.Parcelable
import androidx.annotation.Keep
import com.codebrew.clikat.data.model.api.TrackDhl
import com.codebrew.clikat.modal.other.ProductDataBean
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class OrderHistory(
        val agent: List<Agent?>? = null,
        val for_edit_order_table_booking_discount: Float? = null,
        val for_edit_order_table_booking_price: Float? = null,
        val area_id: Int? = null,
        val comment: String? = null,
        val created_on: String? = null,
        var delivered_on: String? = null,
        val delivery_address: DeliveryAddress? = null,
        val no_touch_delivery: String? = null,
        val delivery_charges: Float? = null,
        val tip_agent: Float? = null,
        val have_pet: Int? = null,
        val parking_instructions: String? = null,
        val cleaner_in: Int? = null,
        var is_appointment: String? = null,
        val area_to_focus: String? = null,
        val duration: Int? = null,
        val from_address: String? = null,
        val from_latitude: Double? = null,
        val from_longitude: Double? = null,
        var handling_admin: Float? = null,
        val handling_supplier: Float? = null,
        val logo: String? = null,
        val near_on: String? = null,
        var net_amount: Float? = null,
        val random_order_id: String? = null,
        val order_id: Int? = null,
        var type: Int? = null,
        var type_copy: Int? = null,
        val terminology: String? = null,
        var orderStatus: String? = null,
        var addOn: Float? = null,
        var total_order_price: Float? = null,
        var questions: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val user_service_charge: Double? = null,
        var slot_price: Float? = null,
        val order_price: Float? = null,
        val payment_type: Int? = null,
        val loyality_point_discount: Float? = null,
        var product: List<ProductDataBean?>? = null,
        var product_count: Int? = null,
        val branch_address: String? = null,
        val progress_on: String? = null,
        val confirmed_on: String? = null,
        val rating: Float? = null,
        val remarks: String? = null,
        val schedule_order: Int? = null,
        var self_pickup: Int? = null,
        val service_date: String? = null,
        val schedule_end_date: String? = null,
        val payment_source: String? = null,
        val shipped_on: String? = null,
        var pres_description: String? = null,
        var pres_image1: String? = null,
        var pres_image2: String? = null,
        var pres_image3: String? = null,
        var pres_image4: String? = null,
        var pres_image5: String? = null,
        var status: Double? = null,
        val supplier_address: String? = null,
        val supplier_branch_id: Int? = null,
        val referral_amount: Float? = null,
        val supplier_id: Int? = null,
        val supplier_name: String? = null,
        val to_address: String? = null,
        val to_latitude: Double? = null,
        val to_longitude: Double? = null,
        val user_delivery_address: Int? = null,
        val isFrom: Int? = null,
        val is_schedule: String? = null,
        val discountAmount: Float? = null,
        val shippingData: ArrayList<OrderHistory>? = null,
        val promoCode: String? = null,
        val approve_rejection_reason: String? = null,
        val created_by: Int? = null,
        val edit_by: String? = null,
        val gift_amount: Float? = null,
        val is_edit: Int? = null,
        val is_supplier_rated: Int? = null,
        val payment_after_confirmation: Int? = null,
        val payment_status: Int? = null,
        val preparation_time: String? = null,
        val refund_amount: Float? = null,
        val remaining_amount: Float? = null,
        val zelle_receipt_url: String? = null,
        val donate_to_someone: Int? = null,
        val delivery_min_time: Int? = null,
        val delivery_max_time: Int? = null,
        var wallet_discount_amount: Float? = null,
        var editOrder: Boolean? = false,
        var table_details: TableDetails? = null,
        var proxy_phone_number: String? = null,
        var nextStatusToChange: Double? = null,
        var have_coin_change: String? = null,
        var shipping_charge: String? = null,
        var package_charge: String? = null,
        var chargeabl_weight: String? = null,
        var airway_bill_number: String? = null,
        var dhlData: ArrayList<OrderHistory>? = null,
        var dhlTrackData: TrackDhl? = null,
        var local_currency: String? = null,
        var currency_exchange_rate: Float? = null,
        var agent_verification_code: String? = null,
        var drop_off_date: String? = null,
        var admin_price_update_receipt: String? = null,
        var user_on_the_way: String? = null,
        var is_shiprocket_assigned: String? = null,
        var admin_updated_charge: Float? = null,
        var is_subtotal_add: String? = null,
        var zoom_call_url: String? = null,
        var zoom_call_start_url: String? = null,
        var order_delivery_type: String? = null,
        var vehicle_number: String? = null,
        var is_cutlery_required: String? = null,
        var is_payment_confirmed: String? = null,
        var delivery_company_id: String? = null,
        var seating_capacity: Int? = null

) : Parcelable

@Parcelize
data class TableDetails(
        val id: Int? = null,
        val table_name: String? = null,
        val qr_code: String? = null,
        val table_number: Int? = null,
        val supplier_id: Int? = null,
        val branch_id: Int? = null,
        val seating_capacity: Int? = null,
        val created_at: String? = null,
        val is_deleted: Int? = null,
        var table_booking_price: Float? = null,
        val updated_at: String? = null
) : Parcelable
