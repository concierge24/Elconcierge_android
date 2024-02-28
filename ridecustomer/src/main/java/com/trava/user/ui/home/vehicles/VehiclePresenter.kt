package com.trava.user.ui.home.vehicles

import com.trava.user.webservices.API
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import com.trava.utilities.webservices.models.LoginModel
import com.trava.utilities.webservices.models.Service
import com.google.android.gms.maps.model.LatLng
import com.trava.user.utils.ConfigPOJO
import com.trava.user.webservices.models.ServiceRequestModel
import com.trava.user.webservices.models.homeapi.ServiceDetails
import com.trava.utilities.Logger
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class VehiclePresenter : BasePresenterImpl<VehicleContract.View>(), VehicleContract.Presenter {
    private var api: Call<ApiResponse<ServiceDetails>>? = null

    override fun getOrderDistance(sourceLatLng: LatLng?, destLatLng: LatLng?, language: String?) {
        getView()?.showLoader(false)
        val BASE_URL_for_map = "https://maps.googleapis.com/maps/api/"
        val retrofit = Retrofit.Builder().baseUrl(BASE_URL_for_map).addConverterFactory(GsonConverterFactory.create()).build()
        val api = retrofit.create(API::class.java)
        var key ="directions/json?sensor=false&mode=driving&alternatives=true&units=metric&key=" + ConfigPOJO.SECRET_API_KEY
        val service = api.getPolYLine(key,sourceLatLng?.latitude.toString() + "," + sourceLatLng?.longitude, destLatLng?.latitude.toString() + "," + destLatLng?.longitude, language)
        service.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                getView()?.showLoader(false)
                if (response.isSuccessful) {
                    try {
                        val responsePolyline = response.body()?.string()
                        val jsonRootObject = JSONObject(responsePolyline)
                        getView()?.orderDistanceSuccess(jsonRootObject)
                    } catch (e: IOException) {
                        e.printStackTrace()

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    val errorModel = getApiError(response.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }

}