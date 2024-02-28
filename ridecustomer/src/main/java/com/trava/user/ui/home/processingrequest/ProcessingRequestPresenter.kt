package com.trava.user.ui.home.processingrequest

import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.user.webservices.models.order.Order
import com.trava.utilities.StatusCode
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigInteger

class ProcessingRequestPresenter : BasePresenterImpl<ProcessingRequestContract.View>(), ProcessingRequestContract.Presenter {
    override fun requestVehicleBreakDownCancelByUser(orderId: BigInteger) {
        getView()?.showLoader(true)
        RestClient.get().cancleVehicleBreakdownByUser(orderId).enqueue(object : Callback<ApiResponse<Any>> {
            override fun onResponse(call: Call<ApiResponse<Any>>?, response: Response<ApiResponse<Any>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.onCancelVehicleBreakdownSuccess()
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }

    override fun requestHafWaycancelByUser(orderId: BigInteger) {
        getView()?.showLoader(true)
        RestClient.get().cancleHalfwayStopByUser(orderId).enqueue(object : Callback<ApiResponse<Any>> {
            override fun onResponse(call: Call<ApiResponse<Any>>?, response: Response<ApiResponse<Any>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.onCancelHalfWayStopSucess()
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })

    }

    override fun requestCancelApiCall(map: HashMap<String, String>) {
        getView()?.showLoader(true)
        RestClient.get().cancelService(map).enqueue(object : Callback<ApiResponse<Any>> {

            override fun onResponse(call: Call<ApiResponse<Any>>?, response: Response<ApiResponse<Any>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.onApiSuccess()
                    } else {
                        val errorBody = response?.errorBody()?.string()
                        val errorResponse: ApiResponse<Order> = Gson().fromJson(errorBody, object : TypeToken<ApiResponse<Order>>() {}.type)
                        if(errorResponse.statusCode == StatusCode.BAD_REQUEST && errorResponse.result != null) {
                            getView()?.handleOrderError(errorResponse.result)
                        }else{
                            val errorModel = getApiError(errorBody)
                            getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                        }
                    }
                } else {
                    val errorBody = response?.errorBody()?.string()
                    val errorResponse: ApiResponse<Order> = Gson().fromJson(errorBody, object : TypeToken<ApiResponse<Order>>() {}.type)
                    if(errorResponse.statusCode == StatusCode.BAD_REQUEST && errorResponse.result != null) {
                        getView()?.handleOrderError(errorResponse.result)
                    }else{
                        val errorModel = getApiError(errorBody)
                        getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse<Any>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }

}