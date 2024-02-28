package com.trava.user

import com.google.android.gms.maps.model.LatLng
import com.trava.user.utils.ConfigPOJO
import com.trava.user.webservices.API
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.user.webservices.models.applabels.AppLabels
import com.trava.user.webservices.models.applabels.Data
import com.trava.user.webservices.models.appsettings.SettingItems
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class MainActivityPresenter : BasePresenterImpl<MainActivityContract.View>(), MainActivityContract.Presenter {

    override fun appSetting()
    {
        getView()?.showLoader(true)
        RestClient.recreate().get().appSettingApi().enqueue(object : Callback<ApiResponse<SettingItems>> {

            override fun onResponse(call: Call<ApiResponse<SettingItems>>?, response: Response<ApiResponse<SettingItems>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.onApiSuccess(response.body()?.result)
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<SettingItems>>?, t: Throwable?) {
                getView()?.apiFailure()
                getView()?.showLoader(false)
            }
        })
    }


    override fun getCurrencyPrice() {
        getView()?.showLoader(true)
        val BASE_URL_for_map = "https://free.currconv.com/api/"
        val retrofit = Retrofit.Builder().baseUrl(BASE_URL_for_map).addConverterFactory(GsonConverterFactory.create()).build()
        val api = retrofit.create(API::class.java)
        var key ="v7/convert?q=AED_OMR&compact=ultra&apiKey=5cda05ab4b542c64e4f1"
        val service = api.getOmanCurrency(key)
        service.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                getView()?.showLoader(false)
                if (response.isSuccessful) {
                    try {
                        val responsePolyline = response.body()?.string()
                        val jsonRootObject = JSONObject(responsePolyline)
                        getView()?.onApiSuccess(jsonRootObject)
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

    override fun getAppLables()
    {
        getView()?.showLoader(true)
        RestClient.recreate().get().getAppLables().enqueue(object : Callback<AppLabels<Data>> {

            override fun onResponse(call: Call<AppLabels<Data>>?, response: Response<AppLabels<Data>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.onAppLablesSuccess(response.body()?.data)
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<AppLabels<Data>>?, t: Throwable?) {
                getView()?.apiFailure()
                getView()?.showLoader(false)
            }
        })
    }

}