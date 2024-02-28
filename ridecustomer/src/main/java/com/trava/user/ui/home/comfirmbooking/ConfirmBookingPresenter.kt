package com.trava.user.ui.home.comfirmbooking

import android.util.Log
import com.google.gson.Gson
import com.trava.driver.webservices.models.WalletBalModel
import com.trava.user.ui.home.orderdetails.omcoproducts.ProductPakageRequest
import com.trava.user.utils.AppUtils
import com.trava.user.utils.ConfigPOJO
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.user.webservices.models.ProductCheckListModel
import com.trava.user.webservices.models.ProductListModel
import com.trava.user.webservices.models.ServiceRequestModel
import com.trava.user.webservices.models.contacts.RideShareResponse
import com.trava.user.webservices.models.order.Order
import com.trava.user.webservices.models.orderrequest.ResultItem
import com.trava.utilities.BOOKING_TYPE
import com.trava.utilities.Constants
import com.trava.utilities.PaymentType
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ConfirmBookingPresenter : BasePresenterImpl<ConfirmBookingContract.View>(), ConfirmBookingContract.Presenter {

    override fun requestUserCreditPoints() {
        getView()?.showLoader(false)
        RestClient.get().getUserCreditPoints().enqueue(object : Callback<ApiResponse<RideShareResponse>> {
            override fun onFailure(call: Call<ApiResponse<RideShareResponse>>, t: Throwable) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }

            override fun onResponse(call: Call<ApiResponse<RideShareResponse>>, response: Response<ApiResponse<RideShareResponse>>) {
                getView()?.showLoader(false)
                if (response.isSuccessful) {
                    if (response.body()?.statusCode == 200) {
                        response.body()?.result?.let { getView()?.onCreditPointsSucess(it) }
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

        })
    }

    override fun getWalletBalance(isLoader: Boolean) {
        if (isLoader)
            getView()?.showLoader(true)
        RestClient.get().getWalletBalance()
                .enqueue(object : Callback<ApiResponse<WalletBalModel?>> {
                    override fun onResponse(call: Call<ApiResponse<WalletBalModel?>>?, response: Response<ApiResponse<WalletBalModel?>>?) {
                        getView()?.showLoader(false)
                        if (response?.isSuccessful == true) {
                            getView()?.onWalletBalSuccess(response.body()?.result)
                        } else {
                            val errorModel = getApiError(response?.errorBody()?.string())
                            getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<WalletBalModel?>>?, t: Throwable?) {
                        getView()?.showLoader(false)
                        getView()?.apiFailure()
                    }
                })
    }

    override fun requestServiceApiCall(requestModel: ServiceRequestModel) {
        getView()?.showLoader(true)
        val orderImages: ArrayList<MultipartBody.Part> = ArrayList()

        val map = HashMap<String, RequestBody>()

        map["category_id"] = RequestBody.create(MultipartBody.FORM, requestModel.category_id.toString())
        map["category_brand_id"] = RequestBody.create(MultipartBody.FORM, requestModel.category_brand_id.toString())
        map["category_brand_product_id"] = RequestBody.create(MultipartBody.FORM, requestModel.category_brand_product_id.toString())
        map["product_quantity"] = RequestBody.create(MultipartBody.FORM, requestModel.product_quantity.toString())
        map["pickup_address"] = RequestBody.create(MultipartBody.FORM, requestModel.pickup_address.toString())
        map["pickup_latitude"] = RequestBody.create(MultipartBody.FORM, requestModel.pickup_latitude.toString())
        map["pickup_longitude"] = RequestBody.create(MultipartBody.FORM, requestModel.pickup_longitude.toString())
        map["dropoff_address"] = RequestBody.create(MultipartBody.FORM, requestModel.dropoff_address.toString())
        map["dropoff_latitude"] = RequestBody.create(MultipartBody.FORM, requestModel.dropoff_latitude.toString())
        map["dropoff_longitude"] = RequestBody.create(MultipartBody.FORM, requestModel.dropoff_longitude.toString())
        map["address_name"] = RequestBody.create(MultipartBody.FORM, requestModel.addressName.toString())
        map["order_timings"] = RequestBody.create(MultipartBody.FORM, requestModel.order_timings.toString())
        map["commercial_value"] = RequestBody.create(MultipartBody.FORM, requestModel.commercial_value.toString())
        map["delivery_type"] = RequestBody.create(MultipartBody.FORM, requestModel.delivery_type.toString())
        map["delivery_person_phone"] = RequestBody.create(MultipartBody.FORM, requestModel.delivery_person_phone.toString())
        map["product_actual_value"] = RequestBody.create(MultipartBody.FORM, (requestModel.selectedProduct?.actual_value?:0f).toString())
        if (ConfigPOJO.TEMPLATE_CODE != Constants.CORSA) {
            if (requestModel.future == "1") {
                if (ConfigPOJO.settingsResponse?.key_value?.is_manual_assignment_order == "true") {
                    map["is_manual_assignment"] = RequestBody.create(MultipartBody.FORM, ConfigPOJO.settingsResponse?.key_value?.is_manual_assignment_order?:"")
                }
            } else {
                map["is_manual_assignment"] = RequestBody.create(MultipartBody.FORM, requestModel.is_manual_assignment?:"")
            }
        }

        map["future"] = RequestBody.create(MultipartBody.FORM, requestModel.future.toString())
        map["payment_type"] = RequestBody.create(MultipartBody.FORM, requestModel.payment_type.toString())
        map["distance"] = RequestBody.create(MultipartBody.FORM, requestModel.distance.toString())
        map["organisation_coupon_user_id"] = RequestBody.create(MultipartBody.FORM, requestModel.organisation_coupon_user_id.toString())
        map["product_weight"] = RequestBody.create(MultipartBody.FORM, requestModel.product_weight.toString())
        map["details"] = RequestBody.create(MultipartBody.FORM, requestModel.details.toString())
        map["material_details"] = RequestBody.create(MultipartBody.FORM, requestModel.material_details.toString())
        map["order_distance"] = RequestBody.create(MultipartBody.FORM, requestModel.order_distance.toString())
        map["pickup_person_name"] = RequestBody.create(MultipartBody.FORM, requestModel.pickup_person_name.toString())
        map["pickup_person_phone"] = RequestBody.create(MultipartBody.FORM, requestModel.pickup_person_phone.toString())
        map["invoice_number"] = RequestBody.create(MultipartBody.FORM, requestModel.invoice_number.toString())
        map["delivery_person_name"] = RequestBody.create(MultipartBody.FORM, requestModel.delivery_person_name.toString())
        map["credit_point_used"] = RequestBody.create(MultipartBody.FORM, requestModel.credit_point_used.toString())
        map["numberOfRiders"] = RequestBody.create(MultipartBody.FORM, requestModel.pool.toString())
        map["user_detail_id"] = RequestBody.create(MultipartBody.FORM, requestModel.user_detail_id.toString())
        map["airport_charges"] = RequestBody.create(MultipartBody.FORM, requestModel.airportCharges
                ?: "")
        map["insurance_amount"] = RequestBody.create(MultipartBody.FORM, requestModel.insurance_amount
                ?: "0")
        map["exact_path"] = RequestBody.create(MultipartBody.FORM, requestModel.directional_path
                ?: "")
        map["is_children"] = RequestBody.create(MultipartBody.FORM, (requestModel.is_children
                ?: "0").toString())
        map["helper"] = RequestBody.create(MultipartBody.FORM, (requestModel.helper ?: "false").toString())

        map["order_time"] = RequestBody.create(MultipartBody.FORM, requestModel.eta.toString())
        var gender = ""
        if (requestModel.gender != "" && requestModel.gender != "null") {
            gender = requestModel.gender
            map["gender"] = RequestBody.create(MultipartBody.FORM, gender)
        }

        if (!requestModel.elevator_pickup.toString().isNullOrEmpty()) {
            map["elevator_pickup"] = RequestBody.create(MultipartBody.FORM, requestModel.elevator_pickup.toString())
        } else {
            map["elevator_pickup"] = RequestBody.create(MultipartBody.FORM, "")
        }
        if (requestModel.pickup_level.toString() != "null" && requestModel.pickup_level.toString() != "") {
            map["pickup_level"] = RequestBody.create(MultipartBody.FORM, requestModel.pickup_level?.toString()?:"")

        } else {
            map["pickup_level"] = RequestBody.create(MultipartBody.FORM, "")
        }
        if (requestModel.dropoff_level.toString() != "null" && requestModel.dropoff_level.toString() != "") {
            map["dropoff_level"] = RequestBody.create(MultipartBody.FORM, requestModel.dropoff_level?.toString()?:"")
        } else {
            map["dropoff_level"] = RequestBody.create(MultipartBody.FORM, "")
        }
        if (ConfigPOJO.TEMPLATE_CODE == Constants.GOMOVE_BASE) {
            if (!requestModel.elevator_dropoff.toString().isNullOrEmpty()) {
                map["elevator_dropoff"] = RequestBody.create(MultipartBody.FORM, requestModel.elevator_dropoff.toString())

            } else {
                map["elevator_dropoff"] = RequestBody.create(MultipartBody.FORM, "")
            }

            if (!requestModel.parking.toString().isNullOrEmpty()) {
                map["parking"] = RequestBody.create(MultipartBody.FORM, requestModel.parking.toString())
            } else {
                map["parking"] = RequestBody.create(MultipartBody.FORM, "")
            }
            if (!requestModel.fragile.toString().isNullOrEmpty()) {
                if (requestModel.fragile == "false") {
                    map["fragile"] = RequestBody.create(MultipartBody.FORM, "0")
                } else if (requestModel.fragile == "true") {
                    map["fragile"] = RequestBody.create(MultipartBody.FORM, "1")
                } else {
                    map["fragile"] = RequestBody.create(MultipartBody.FORM, requestModel.fragile.toString())
                }
            } else {
                map["parking"] = RequestBody.create(MultipartBody.FORM, "")
            }
        }

        if (ConfigPOJO.is_merchant == "true") {
            map["km_bonus"] = RequestBody.create(MultipartBody.FORM, requestModel.km_bonus.toString())
        }

        if (requestModel.isPromoApplied) {
            /* When PromoCode Applied */
//            map["coupon_id"] = RequestBody.create(MultipartBody.FORM,requestModel.coupenId.toString())
            map["coupon_code"] = RequestBody.create(MultipartBody.FORM, requestModel.couponcode.toString())
            if (requestModel.credit_point_used != 0) {
                map["final_charge"] = RequestBody.create(MultipartBody.FORM, AppUtils.getFormattedDecimal(requestModel.afterPromoFinalCharge?.minus(requestModel.credit_point_used?.toDouble()
                        ?: 0.0)
                        ?: 0.0).toString())
            } else {
                map["final_charge"] = RequestBody.create(MultipartBody.FORM, AppUtils.getFormattedDecimal(requestModel.afterPromoFinalCharge
                        ?: 0.0).toString())
            }
        }

        if (requestModel.pkgData != null) {
            /* When Package booking made */
            map["package_id"] = RequestBody.create(MultipartBody.FORM, requestModel.pkgData?.packageId.toString())
            map["price_per_min"] = RequestBody.create(MultipartBody.FORM, requestModel.price_per_min.toString())
            map["time_fixed_price"] = RequestBody.create(MultipartBody.FORM, requestModel.time_fixed_price.toString())
            map["distance_price_fixed"] = RequestBody.create(MultipartBody.FORM, requestModel.distance_price_fixed.toString())
            map["price_per_km"] = RequestBody.create(MultipartBody.FORM, requestModel.price_per_km.toString())
        }
        when (requestModel.bookingType) {
            BOOKING_TYPE.FRIEND -> {
                /* When book for a friend booking made  */
                map["booking_type"] = RequestBody.create(MultipartBody.FORM, requestModel.bookingType)
                map["friend_name"] = RequestBody.create(MultipartBody.FORM, requestModel.bookingFriendName)
                map["relation"] = RequestBody.create(MultipartBody.FORM, requestModel.bookingFriendRelation)
                map["friend_phone_number"] = RequestBody.create(MultipartBody.FORM, requestModel.bookingFriendPhoneNumber)
                map["friend_phone_code"] = RequestBody.create(MultipartBody.FORM, requestModel.bookingFriendCountryCode)
            }
            BOOKING_TYPE.GIFT -> {
                map["isGifted"] = RequestBody.create(MultipartBody.FORM, requestModel.isGifted)
                map["friend_name"] = RequestBody.create(MultipartBody.FORM, requestModel.bookingFriendName)
                map["friend_phone_number"] = RequestBody.create(MultipartBody.FORM, requestModel.bookingFriendPhoneNumber)
                map["friend_phone_code"] = RequestBody.create(MultipartBody.FORM, requestModel.bookingFriendCountryCode)
            }
            BOOKING_TYPE.ROAD_PICKUP -> {
                /* When road pickup booking made */
                map["booking_type"] = RequestBody.create(MultipartBody.FORM, requestModel.bookingType)
                map["driver_id"] = RequestBody.create(MultipartBody.FORM, requestModel.driverId.toString())
            }
            BOOKING_TYPE.CARPOOL -> {
                map["booking_type"] = RequestBody.create(MultipartBody.FORM, requestModel.bookingType)
            }
        }


        if (requestModel.images.isNotEmpty()) {
            requestModel.images.forEach {
                val file = File(it)
                val image = RequestBody.create("image/jpeg".toMediaTypeOrNull(), file)
                val data = MultipartBody.Part.createFormData("order_images",
                        file.name, image)
                orderImages.add(data)
            }
        }

        if (requestModel.stops.isNotEmpty()) {
            map["stops"] = RequestBody.create(MultipartBody.FORM, Gson().toJson(requestModel.stops))
        }

        if (requestModel.check_lists.isNotEmpty()) {
            map["check_lists"] = RequestBody.create(MultipartBody.FORM, Gson().toJson(requestModel.check_lists))
        }

        if (requestModel.cancellation_charges ?: 0.0 > 0.0) {
            map["cancellation_charges"] = RequestBody.create(MultipartBody.FORM, requestModel.cancellation_charges.toString())
            map["cancellation_payment_id"] = RequestBody.create(MultipartBody.FORM, requestModel.cancellation_payment_id.toString())
            if (requestModel.credit_point_used != 0) {
                map["final_charge"] = RequestBody.create(MultipartBody.FORM, requestModel.final_charge?.plus(requestModel.cancellation_charges
                        ?: 0.0)?.minus(requestModel.credit_point_used?.toDouble()
                        ?: 0.0).toString())
            } else {
                map["final_charge"] = RequestBody.create(MultipartBody.FORM, requestModel.final_charge?.plus(requestModel.cancellation_charges
                        ?: 0.0).toString())
            }
        }

        if (requestModel.payment_type == PaymentType.CARD) {
            /* Payment by card */
            map["user_card_id"] = RequestBody.create(MultipartBody.FORM, requestModel.user_card_id.toString())
        }

        if (!map.containsKey("final_charge")) {
            if (requestModel.credit_point_used != 0) {
                map["final_charge"] = RequestBody.create(MultipartBody.FORM, AppUtils.getFormattedDecimal(requestModel.final_charge?.minus(requestModel.credit_point_used?.toDouble()
                        ?: 0.0) ?: 0.0).toString())
            } else {
                map["final_charge"] = RequestBody.create(MultipartBody.FORM, AppUtils.getFormattedDecimal(requestModel.final_charge
                        ?: 0.0).toString())
            }
        }

        Log.e("book request", map.toString())

        RestClient.get().requestService(map, orderImages).enqueue(object : Callback<ApiResponse<Order>> {
            override fun onResponse(call: Call<ApiResponse<Order>>?, response: Response<ApiResponse<Order>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.onApiSuccess(response.body()?.result)
                    } else {
                        if (response.body()?.statusCode == 301) {
                            getView()?.onOutstandingCharges(response.body()?.result)
                        } else {
                            getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                        }
                    }
                } else {
                    if (response?.errorBody() != null) {
                        val errorModel = getApiError(response.errorBody()?.string())
                        getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                    } else {
                        getView()?.handleApiError(0, "Server error accured")
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse<Order>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }

    override fun requestPickupApi(requestModel: ServiceRequestModel, productList: ArrayList<ProductListModel>, productCheckList: ArrayList<ProductCheckListModel>) {
        getView()?.showLoader(true)

        var gson = Gson()
        var productPckageRequest = ProductPakageRequest(requestModel?.pickup_address!!, requestModel?.pickup_latitude.toString(), requestModel?.pickup_longitude.toString(),
                requestModel?.selected_address_id.toString(), requestModel?.dropoff_address.toString(), requestModel?.dropoff_latitude.toString(),
                requestModel?.dropoff_longitude.toString(), productList, requestModel?.category_id.toString(), requestModel?.category_brand_id.toString(), requestModel?.category_brand_product_id.toString(),
                requestModel?.bookingType, "")

        RestClient.get().requestPickupReceiveService(productPckageRequest).enqueue(object : Callback<ApiResponse<ResultItem>> {

            override fun onResponse(call: Call<ApiResponse<ResultItem>>?, response: Response<ApiResponse<ResultItem>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.onRequestApiSuccess(response.body()?.result!!)
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    if (response?.errorBody() != null) {
                        val errorModel = getApiError(response.errorBody()?.string())
                        getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                    } else {
                        getView()?.handleApiError(0, "Server error accured")
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse<ResultItem>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }
}