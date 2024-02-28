package com.trava.user.ui.home

import android.util.Log
import com.trava.user.webservices.API
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import com.trava.utilities.webservices.models.LoginModel
import com.trava.utilities.webservices.models.Service
import com.google.android.gms.maps.model.LatLng
import com.trava.user.utils.ConfigPOJO
import com.trava.user.webservices.ApiResponseNew
import com.trava.user.webservices.models.GeoFenceData
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
import java.lang.Exception

class HomePresenter : BasePresenterImpl<HomeContract.View>(), HomeContract.Presenter {
    private var api: Call<ApiResponse<ServiceDetails>>? = null

    override fun onGetPolygon() {
//        getView()?.showLoader(true)
        RestClient.get().getPolygon().enqueue(object : Callback<ApiResponse<ArrayList<GeoFenceData>>> {
            override fun onResponse(call: Call<ApiResponse<ArrayList<GeoFenceData>>>?, response: Response<ApiResponse<ArrayList<GeoFenceData>>>?) {
//                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.onPolygonDataSuccess(response.body()?.result)
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<ArrayList<GeoFenceData>>>?, t: Throwable?) {
//                getView()?.showLoader(false)
                getView()?.apiFailure()
            }

        })
    }

    override fun homeApi(map: Map<String, String>) {
        getView()?.showLoader(false)
        api?.cancel()
        api = RestClient.recreate().get().homeApi(map)
        api?.enqueue(object : Callback<ApiResponse<ServiceDetails>> {

            override fun onResponse(call: Call<ApiResponse<ServiceDetails>>?, response: Response<ApiResponse<ServiceDetails>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.onHomeApiSuccess(response.body()?.result)
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<ServiceDetails>>?, t: Throwable?) {
                if (t?.message.toString() != "Canceled" && t?.message.toString() != "Socket closed") {
                    getView()?.showLoader(false)
                    getView()?.apiFailure()
                    Logger.e("homeApi?Error", t?.message.toString())
                }
            }
        })
    }

    override fun updateDataApi(map: HashMap<String, String>) {
        RestClient.get().updateData(map).enqueue(object : Callback<ApiResponse<LoginModel>> {
            override fun onResponse(call: Call<ApiResponse<LoginModel>>?, response: Response<ApiResponse<LoginModel>>?) {
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.onApiSuccess(response.body()?.result)
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    try {
                        val errorModel = getApiError(response?.errorBody()?.string())
                        getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                    }catch (exp: Exception){
                        Log.e("Exception : ",exp.message?:"")
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse<LoginModel>>?, t: Throwable?) {
                getView()?.apiFailure()
            }
        })
    }

    override fun logout() {
        getView()?.showLoader(true)
        RestClient.get().logout().enqueue(object : Callback<Any>{

            override fun onResponse(call: Call<Any>?, response: Response<Any>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true){
                    getView()?.logoutSuccess()
                }else{
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<Any>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }

    override fun getSupportList() {
//        getView()?.showLoader(true)
//        RestClient.get().supportList().enqueue(object : Callback<ApiResponse<ArrayList<Service>>> {
//            override fun onResponse(call: Call<ApiResponse<ArrayList<Service>>>?, response: Response<ApiResponse<ArrayList<Service>>>?) {
//                getView()?.showLoader(false)
//                if (response?.isSuccessful == true) {
//                    if (response.body()?.statusCode == 200) {
//                        getView()?.onSupportListApiSuccess(response.body()?.result)
//                    } else {
//                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
//                    }
//                } else {
//                    val errorModel = getApiError(response?.errorBody()?.string())
//                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
//                }
//            }
//
//            override fun onFailure(call: Call<ApiResponse<ArrayList<Service>>>?, t: Throwable?) {
//                getView()?.showLoader(false)
//                getView()?.apiFailure()
//            }
//
//        })
    }

    override fun drawPolyLine(sourceLatLng: LatLng?, destLatLng: LatLng?, language: String?) {
        getView()?.showLoader(true)
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
                        getView()?.polyLine(jsonRootObject, sourceLatLng, destLatLng)
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

    override fun nearByPlaces(sourceLatLng: LatLng?, destLatLng: LatLng?, language: String?) {
        getView()?.showLoader(true)
        val BASE_URL_for_map = "https://maps.googleapis.com/maps/api/"
        val retrofit = Retrofit.Builder().baseUrl(BASE_URL_for_map).addConverterFactory(GsonConverterFactory.create()).build()
        val api = retrofit.create(API::class.java)
        var key ="place/nearbysearch/json?location="+sourceLatLng?.latitude.toString() + "," + sourceLatLng?.longitude +"&radius=8000&sensor=true&key="+ ConfigPOJO.SECRET_API_KEY
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

    override fun getDriversDistanceTime(sourceLatLng: LatLng?, destLatLng: LatLng?, language: String?) {
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
                        getView()?.driverDistanceTimeSuccess(jsonRootObject)
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

    override fun drawPolyLineWithWayPoints(model: ServiceRequestModel,sourceLatLng: LatLng?, destLatLng: LatLng?) {
        getView()?.showLoader(true)
        val BASE_URL_for_map = "https://maps.googleapis.com/maps/api/directions/json?sensor=false&mode=driving&alternatives=true&units=metric&key="+ConfigPOJO.SECRET_API_KEY+"&origin="
        val retrofit = Retrofit.Builder().baseUrl("https://maps.googleapis.com/maps/api/").addConverterFactory(GsonConverterFactory.create()).build()
        val api = retrofit.create(API::class.java)
        var url = StringBuilder("")
        if(model.stops.size>0)
        {
            url.append(BASE_URL_for_map+sourceLatLng?.latitude+","+sourceLatLng?.longitude+"&destination="
                    +destLatLng?.latitude+","+destLatLng?.longitude+"&waypoints=via:")
            for (i in model.stops.indices)
            {
                val coordinates=String.format("%s",model.stops[i].latitude.toString()+","+model.stops[i].longitude+"|via:")
                url.append(coordinates)
            }
        }
        val service = api.getPolyLine(url.toString())
        service.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                getView()?.showLoader(false)
                if (response.isSuccessful) {
                    try {
                        val responsePolyline = response.body()?.string()
                        val jsonRootObject = JSONObject(responsePolyline)
                        getView()?.polyLine(jsonRootObject, LatLng(model.pickup_latitude?:0.0,model.pickup_longitude?:0.0), LatLng(model.dropoff_latitude?:0.0,model.dropoff_longitude?:0.0))
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


    override fun saveRazorPaymentData(map: HashMap<String, Any>) {
        getView()?.showLoader(true)
        RestClient.recreate().get().saveRazorPaymentData(map).enqueue(object : Callback<ApiResponseNew> {

            override fun onResponse(call: Call<ApiResponseNew>?, response: Response<ApiResponseNew>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.RazorPaymentApiSuccess(response.body())
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponseNew>?, t: Throwable?) {
                getView()?.apiFailure()
                getView()?.showLoader(false)
            }
        })
    }


    override fun saveDatatranseData(map: HashMap<String, Any>) {
        getView()?.showLoader(true)
        RestClient.recreate().get().saveThiwaniData(map).enqueue(object : Callback<ApiResponseNew> {

            override fun onResponse(call: Call<ApiResponseNew>?, response: Response<ApiResponseNew>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.DataThawaniSuccess(response.body())
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    getView()?.handleApiError(404, "Not Found")
                }
            }

            override fun onFailure(call: Call<ApiResponseNew>?, t: Throwable?) {
                getView()?.apiFailure()
                getView()?.showLoader(false)
            }
        })
    }


}