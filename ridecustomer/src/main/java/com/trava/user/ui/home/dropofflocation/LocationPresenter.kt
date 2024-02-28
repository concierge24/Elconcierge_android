package com.trava.user.ui.home.dropofflocation

import com.trava.user.webservices.API
import com.trava.user.webservices.ApiResponse
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import com.google.android.gms.maps.model.LatLng
import com.trava.user.utils.ConfigPOJO
import com.trava.user.webservices.models.homeapi.ServiceDetails
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class LocationPresenter : BasePresenterImpl<LocationContract.View>(), LocationContract.Presenter {
    private var api: Call<ApiResponse<ServiceDetails>>? = null

    override fun nearByPlaces(sourceLatLng: LatLng?, destLatLng: LatLng?, language: String?, type: String) {
        getView()?.showLoader(true)
        val BASE_URL_for_map = "https://maps.googleapis.com/maps/api/"
        val retrofit = Retrofit.Builder().baseUrl(BASE_URL_for_map).addConverterFactory(GsonConverterFactory.create()).build()
        val api = retrofit.create(API::class.java)
        var key = "place/nearbysearch/json?location=" + sourceLatLng?.latitude.toString() + "," + sourceLatLng?.longitude + "&radius=10000&type=" + type + "&sensor=true&key=" + ConfigPOJO.SECRET_API_KEY
        val service = api.getPolyLine(key)
        service.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                getView()?.showLoader(false)
                if (response.isSuccessful) {
                    try {
                        val responsePolyline = response.body()?.string()
                        val jsonRootObject = JSONObject(responsePolyline)
                        getView()?.nearByResponse(jsonRootObject, sourceLatLng, destLatLng)
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