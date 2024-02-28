package com.trava.user.ui.home.dropofflocation

import android.widget.Toast
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.user.webservices.models.order.Order
import com.trava.utilities.StatusCode
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.trava.user.webservices.models.add_address_home_work.HomeWorkResponse
import com.trava.user.webservices.models.locationaddress.GetAddressesPojo
import com.trava.user.webservices.models.roadPickup.RoadPickupResponse
import com.trava.utilities.MyApplication
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigInteger

class AddStopsPresenter : BasePresenterImpl<AddStopsContract.View>(), AddStopsContract.Presenter {
    override fun requestEditHomeWorkAddress(map: HashMap<String, String>) {
        getView()?.showLoader(true)
        RestClient.get().editAddressForHomeWork(map).enqueue(object : Callback<ApiResponse<HomeWorkResponse>> {
            override fun onResponse(call: Call<ApiResponse<HomeWorkResponse>>?, response: Response<ApiResponse<HomeWorkResponse>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        response.body()?.result?.let { getView()?.onApiEditHomeWorkSuccess(it) }
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<HomeWorkResponse>>, t: Throwable) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }

    override fun requestHomeWorkAddress(map: HashMap<String, String>) {
        getView()?.showLoader(true)
        RestClient.get().addAddressForHomeWork(map).enqueue(object : Callback<ApiResponse<HomeWorkResponse>> {
            override fun onResponse(call: Call<ApiResponse<HomeWorkResponse>>?, response: Response<ApiResponse<HomeWorkResponse>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        response.body()?.result?.let { getView()?.onApiHomeWorkSuccess(it) }
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<HomeWorkResponse>>, t: Throwable) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }

    override fun requestAddStopsDuringRide(map : HashMap<String,String>) {
        getView()?.showLoader(true)
        RestClient.get().addStopsDuringRide(map).enqueue(object : Callback<ApiResponse<ArrayList<Order>>> {
            override fun onResponse(call: Call<ApiResponse<ArrayList<Order>>>?, response: Response<ApiResponse<ArrayList<Order>>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        if(response.body()?.result?.isNotEmpty() == true) {
                            response.body()?.result?.get(0)?.let { getView()?.onApiSuccess(it) }
                        }else{
                            Toast.makeText(MyApplication.instance,"Something went wrong",Toast.LENGTH_LONG).show()
                        }
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<ArrayList<Order>>>, t: Throwable) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }

        override fun requestLocationsData(map: HashMap<String, String>) {
            getView()?.showLoader(true)
            RestClient.get().requestLocationsData(map).enqueue(object : Callback<GetAddressesPojo> {
                override fun onResponse(call: Call<GetAddressesPojo>?, response: Response<GetAddressesPojo>?) {
                    getView()?.showLoader(false)
                    if (response?.isSuccessful == true) {
                        if (response.body()?.statusCode == 200) {
                            response.body()?.let { getView()?.onApiLocationsSuccess(it) }
                        } else {
                            getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                        }
                    } else {
                        val errorModel = getApiError(response?.errorBody()?.string())
                        getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                    }
                }

                override fun onFailure(call: Call<GetAddressesPojo>, t: Throwable) {
                    getView()?.showLoader(false)
                    getView()?.apiFailure()
                }
            })
        }

    }