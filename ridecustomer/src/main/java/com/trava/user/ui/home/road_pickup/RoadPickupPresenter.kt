package com.trava.user.ui.home.road_pickup

import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.user.webservices.models.order.Order
import com.trava.utilities.StatusCode
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.trava.user.webservices.models.roadPickup.RoadPickupResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RoadPickupPresenter : BasePresenterImpl<RoadPickupContract.View>(), RoadPickupContract.Presenter {

    override fun requestGetDataFromQRCodeApiCall(qrCodeScanUserId: String) {
        getView()?.showLoader(true)
        RestClient.get().getQRCodeScanData(qrCodeScanUserId).enqueue(object : Callback<ApiResponse<RoadPickupResponse>> {

            override fun onResponse(call: Call<ApiResponse<RoadPickupResponse>>?, response: Response<ApiResponse<RoadPickupResponse>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        response.body()?.result?.let { getView()?.onApiSuccess(it) }
                    } else {
                        val errorBody = response.errorBody()?.string()
//                        val errorResponse: ApiResponse<Order> = Gson().fromJson(errorBody, object : TypeToken<ApiResponse<Order>>() {}.type)
                        val errorModel = getApiError(errorBody)
                        getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                    }
                } else {
                    val errorBody = response?.errorBody()?.string()
//                    val errorResponse: ApiResponse<Order> = Gson().fromJson(errorBody, object : TypeToken<ApiResponse<Order>>() {}.type)
                    val errorModel = getApiError(errorBody)
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<RoadPickupResponse>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }
}