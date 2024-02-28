package com.trava.user.webservices.models

import android.os.Parcel
import android.os.Parcelable
import com.trava.user.webservices.models.cards_model.UserCardData
import com.trava.user.webservices.models.homeapi.HomeDriver
import com.trava.user.webservices.models.homeapi.Product
import com.trava.user.webservices.models.promocodes.CouponsItem
import com.trava.user.webservices.models.travelPackages.PackagesItem
import com.trava.utilities.constants.REQUEST_DISTANCE

class ServiceRequestModel() :Parcelable {

    var category_id: Int? = -1
    var helper_percentage: Float? = null
    var category_name: String? = ""
    var category_brand_id: Int? = -1
    var category_brand_product_id: Int? = -1
    var bookingFlow: String? = "0"
    var is_manual_assignment: String? = "false"
    var helper: String? = "false"
    var load_unload_charges: Int? = 0
    var product_quantity: Int? = 1
    var pickup_address: String? = ""
    var airportChargesPickup: String? = ""
    var airportChargesDrop: String? = ""
    var dropoff_address: String? = ""
    var brandName: String? = ""
    var brandImage: String? = ""
    var productName: String? = ""
    var pickup_latitude: Double? = 0.0
    var pickup_longitude: Double? = 0.0
    var dropoff_latitude: Double? = 0.0
    var dropoff_longitude: Double? = 0.0
    var order_timings: String? = ""
    var order_timings_text: String? = ""
    var future: String? = "0"
    var payment_type: String? = ""
    var distance: Int? = REQUEST_DISTANCE
    var price_per_item: Float? = 0f
    var final_charge: Double? = 0.0
    var surChargeValue: Double? = 0.0
    var seat_booked: Int? = 0
    var promoData: CouponsItem? = null
    var organisation_coupon_user_id: Int? = null
    var coupon_user_id: Int? = null
    var user_detail_id: Int? = null
    var isCurrentLocation = false
    var material_details: String? = ""
    var airportCharges: String? = ""
    var insurance_amount: String? = ""
    var directional_path: String? = ""
    var details: String? = ""
    var product_weight: Float? = 0F
    var order_distance: Float? = 0f
    var images: ArrayList<String> = ArrayList()
    var recieverName:String?=""
    var recieverPhone:String?=""
    var reciptDetail:String?=""
    var pickup_person_name:String?=""
    var pickup_person_phone:String?=""
    var invoice_number:String?=""
    var delivery_person_name:String?=""
    var eta : Int? = 0
    var seating_capacity : Int? = 0
    var pkgData : PackagesItem? = null
    var isDriverAccepted : Boolean = false
    var price_per_min : Double = 0.0
    var time_fixed_price : Double = 0.0
    var distance_price_fixed : Double = 0.0
    var price_per_km : Double = 0.0
    var bookingType = ""
    var bookingTypeTemp = ""
    var bookingFriendPhoneNumber = ""
    var bookingFriendRelation = ""
    var bookingFriendName = ""
    var bookingFriendCountryCode = ""
    var selectedProduct : Product? = null
    var driverId : String? = ""
    var userId : String? = ""
    var isPromoApplied : Boolean = false
    var coupenId : Int? = 0
    var couponcode : String? = ""
    var couponType : String? = ""
    var amountValue : String? = ""
    var pool : String? = ""
    var cancellation_payment_id : String? = ""
    var afterPromoFinalCharge: Double? = 0.0
    var cancellation_charges: Double? = 0.0
    var stops = ArrayList<StopsModel>()
    var isLongDistancePickup = false
    var order_status = ""
    var addressName : String? = ""
    var isOutStandingCharges = false
    var driverList  = ArrayList<HomeDriver>()
    var user_card_id : Int? = 0
    var credit_point_used : Int? =0
    var selectedCard : UserCardData? = null
    var elevator_pickup : String? = null
    var commercial_value : String? = null
    var delivery_type : String? = null
    var delivery_person_phone : String? = null
    var elevator_dropoff : String? = null
    var pickup_level : String? = null
    var dropoff_level : String? = null
    var parking : String? = null
    var fragile : String? = null
    var check_lists = ArrayList<CheckListModel>()
    var gender = ""
    var km_bonus = ""
    var km_earned= "0"
    var selected_address_id=""
    var is_children= "0"
    var isGifted = "0"

    constructor(parcel: Parcel) : this() {
        category_id = parcel.readValue(Int::class.java.classLoader) as? Int
        category_name = parcel.readString()
        category_brand_id = parcel.readValue(Int::class.java.classLoader) as? Int
        category_brand_product_id = parcel.readValue(Int::class.java.classLoader) as? Int
        bookingFlow = parcel.readString()
        is_manual_assignment = parcel.readString()
        helper = parcel.readString()
        load_unload_charges = parcel.readValue(Int::class.java.classLoader) as? Int
        product_quantity = parcel.readValue(Int::class.java.classLoader) as? Int
        pickup_address = parcel.readString()
        airportChargesPickup = parcel.readString()
        airportChargesDrop = parcel.readString()
        dropoff_address = parcel.readString()
        brandName = parcel.readString()
        brandImage = parcel.readString()
        productName = parcel.readString()
        pickup_latitude = parcel.readValue(Double::class.java.classLoader) as? Double
        pickup_longitude = parcel.readValue(Double::class.java.classLoader) as? Double
        dropoff_latitude = parcel.readValue(Double::class.java.classLoader) as? Double
        dropoff_longitude = parcel.readValue(Double::class.java.classLoader) as? Double
        order_timings = parcel.readString()
        order_timings_text = parcel.readString()
        future = parcel.readString()
        payment_type = parcel.readString()
        distance = parcel.readValue(Int::class.java.classLoader) as? Int
        price_per_item = parcel.readValue(Float::class.java.classLoader) as? Float
        final_charge = parcel.readValue(Double::class.java.classLoader) as? Double
        surChargeValue = parcel.readValue(Double::class.java.classLoader) as? Double
        seat_booked = parcel.readValue(Int::class.java.classLoader) as? Int
        promoData = parcel.readParcelable(CouponsItem::class.java.classLoader)
        organisation_coupon_user_id = parcel.readValue(Int::class.java.classLoader) as? Int
        coupon_user_id = parcel.readValue(Int::class.java.classLoader) as? Int
        user_detail_id = parcel.readValue(Int::class.java.classLoader) as? Int
        isCurrentLocation = parcel.readByte() != 0.toByte()
        material_details = parcel.readString()
        airportCharges = parcel.readString()
        insurance_amount = parcel.readString()
        directional_path = parcel.readString()
        details = parcel.readString()
        product_weight = parcel.readValue(Float::class.java.classLoader) as? Float
        order_distance = parcel.readValue(Float::class.java.classLoader) as? Float
        recieverName = parcel.readString()
        recieverPhone = parcel.readString()
        reciptDetail = parcel.readString()
        pickup_person_name = parcel.readString()
        pickup_person_phone = parcel.readString()
        invoice_number = parcel.readString()
        delivery_person_name = parcel.readString()
        eta = parcel.readValue(Int::class.java.classLoader) as? Int
        seating_capacity = parcel.readValue(Int::class.java.classLoader) as? Int
        pkgData = parcel.readParcelable(PackagesItem::class.java.classLoader)
        isDriverAccepted = parcel.readByte() != 0.toByte()
        price_per_min = parcel.readDouble()
        time_fixed_price = parcel.readDouble()
        distance_price_fixed = parcel.readDouble()
        price_per_km = parcel.readDouble()
        bookingType = parcel.readString()?:""
        bookingTypeTemp = parcel.readString()?:""
        bookingFriendPhoneNumber = parcel.readString()?:""
        bookingFriendRelation = parcel.readString()?:""
        bookingFriendName = parcel.readString()?:""
        bookingFriendCountryCode = parcel.readString()?:""
        selectedProduct = parcel.readParcelable(Product::class.java.classLoader)
        driverId = parcel.readString()
        userId = parcel.readString()
        isPromoApplied = parcel.readByte() != 0.toByte()
        coupenId = parcel.readValue(Int::class.java.classLoader) as? Int
        couponcode = parcel.readString()
        couponType = parcel.readString()
        amountValue = parcel.readString()
        pool = parcel.readString()
        cancellation_payment_id = parcel.readString()
        afterPromoFinalCharge = parcel.readValue(Double::class.java.classLoader) as? Double
        cancellation_charges = parcel.readValue(Double::class.java.classLoader) as? Double
        isLongDistancePickup = parcel.readByte() != 0.toByte()
        order_status = parcel.readString()?:""
        addressName = parcel.readString()
        isOutStandingCharges = parcel.readByte() != 0.toByte()
        user_card_id = parcel.readValue(Int::class.java.classLoader) as? Int
        credit_point_used = parcel.readValue(Int::class.java.classLoader) as? Int
        selectedCard = parcel.readParcelable(UserCardData::class.java.classLoader)
        elevator_pickup = parcel.readString()
        commercial_value = parcel.readString()
        delivery_type = parcel.readString()
        delivery_person_phone = parcel.readString()
        elevator_dropoff = parcel.readString()
        pickup_level = parcel.readString()
        dropoff_level = parcel.readString()
        parking = parcel.readString()
        fragile = parcel.readString()
        gender = parcel.readString()?:""
        km_bonus = parcel.readString()?:""
        km_earned = parcel.readString()?:""
        selected_address_id = parcel.readString()?:""
        is_children = parcel.readString()?:""
        isGifted = parcel.readString()?:""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(category_id)
        parcel.writeString(category_name)
        parcel.writeValue(category_brand_id)
        parcel.writeValue(category_brand_product_id)
        parcel.writeString(bookingFlow)
        parcel.writeString(is_manual_assignment)
        parcel.writeString(helper)
        parcel.writeValue(load_unload_charges)
        parcel.writeValue(product_quantity)
        parcel.writeString(pickup_address)
        parcel.writeString(airportChargesPickup)
        parcel.writeString(airportChargesDrop)
        parcel.writeString(dropoff_address)
        parcel.writeString(brandName)
        parcel.writeString(brandImage)
        parcel.writeString(productName)
        parcel.writeValue(pickup_latitude)
        parcel.writeValue(pickup_longitude)
        parcel.writeValue(dropoff_latitude)
        parcel.writeValue(dropoff_longitude)
        parcel.writeString(order_timings)
        parcel.writeString(order_timings_text)
        parcel.writeString(future)
        parcel.writeString(payment_type)
        parcel.writeValue(distance)
        parcel.writeValue(price_per_item)
        parcel.writeValue(final_charge)
        parcel.writeValue(surChargeValue)
        parcel.writeValue(seat_booked)
        parcel.writeParcelable(promoData, flags)
        parcel.writeValue(organisation_coupon_user_id)
        parcel.writeValue(coupon_user_id)
        parcel.writeValue(user_detail_id)
        parcel.writeByte(if (isCurrentLocation) 1 else 0)
        parcel.writeString(material_details)
        parcel.writeString(airportCharges)
        parcel.writeString(insurance_amount)
        parcel.writeString(directional_path)
        parcel.writeString(details)
        parcel.writeValue(product_weight)
        parcel.writeValue(order_distance)
        parcel.writeString(recieverName)
        parcel.writeString(recieverPhone)
        parcel.writeString(reciptDetail)
        parcel.writeString(pickup_person_name)
        parcel.writeString(pickup_person_phone)
        parcel.writeString(invoice_number)
        parcel.writeString(delivery_person_name)
        parcel.writeValue(eta)
        parcel.writeValue(seating_capacity)
        parcel.writeParcelable(pkgData, flags)
        parcel.writeByte(if (isDriverAccepted) 1 else 0)
        parcel.writeDouble(price_per_min)
        parcel.writeDouble(time_fixed_price)
        parcel.writeDouble(distance_price_fixed)
        parcel.writeDouble(price_per_km)
        parcel.writeString(bookingType)
        parcel.writeString(bookingTypeTemp)
        parcel.writeString(bookingFriendPhoneNumber)
        parcel.writeString(bookingFriendRelation)
        parcel.writeString(bookingFriendName)
        parcel.writeString(bookingFriendCountryCode)
        parcel.writeParcelable(selectedProduct, flags)
        parcel.writeString(driverId)
        parcel.writeString(userId)
        parcel.writeByte(if (isPromoApplied) 1 else 0)
        parcel.writeValue(coupenId)
        parcel.writeString(couponcode)
        parcel.writeString(couponType)
        parcel.writeString(amountValue)
        parcel.writeString(pool)
        parcel.writeString(cancellation_payment_id)
        parcel.writeValue(afterPromoFinalCharge)
        parcel.writeValue(cancellation_charges)
        parcel.writeByte(if (isLongDistancePickup) 1 else 0)
        parcel.writeString(order_status)
        parcel.writeString(addressName)
        parcel.writeByte(if (isOutStandingCharges) 1 else 0)
        parcel.writeValue(user_card_id)
        parcel.writeValue(credit_point_used)
        parcel.writeParcelable(selectedCard, flags)
        parcel.writeString(elevator_pickup)
        parcel.writeString(commercial_value)
        parcel.writeString(delivery_type)
        parcel.writeString(delivery_person_phone)
        parcel.writeString(elevator_dropoff)
        parcel.writeString(pickup_level)
        parcel.writeString(dropoff_level)
        parcel.writeString(parking)
        parcel.writeString(fragile)
        parcel.writeString(gender)
        parcel.writeString(km_bonus)
        parcel.writeString(km_earned)
        parcel.writeString(selected_address_id)
        parcel.writeString(is_children)
        parcel.writeString(isGifted)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ServiceRequestModel> {
        val BOOK_NOW = "0"
        val SCHEDULE = "1"
        override fun createFromParcel(parcel: Parcel): ServiceRequestModel {
            return ServiceRequestModel(parcel)
        }

        override fun newArray(size: Int): Array<ServiceRequestModel?> {
            return arrayOfNulls(size)
        }
    }


}
