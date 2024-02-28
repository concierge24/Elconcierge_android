package com.trava.user.ui.home.deliverystarts

import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonObject
import com.trava.user.utils.ConfigPOJO
import com.trava.user.webservices.API
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.user.webservices.models.ServiceRequestModel
import com.trava.user.webservices.models.TrackingModel
import com.trava.user.webservices.models.contacts.ContactReqModel
import com.trava.user.webservices.models.contacts.ContactRequestModel
import com.trava.user.webservices.models.contacts.RideShareResponse
import com.trava.user.webservices.models.contacts.ShareRideReqModel
import com.trava.user.webservices.models.nearestroad.RoadPoints
import com.trava.user.webservices.models.order.Order
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
import retrofit2.http.FieldMap
import java.io.IOException
import java.math.BigInteger

class DeliveryStartsPresenter : BasePresenterImpl<DeliveryStartsContract.View>(), DeliveryStartsContract.Presenter {

    override fun requestPanicApi(map: HashMap<String, Any>) {
        getView()?.showLoader(true)
        RestClient.get().panicClick(map).enqueue(object : Callback<ApiResponse<Any>> {
            override fun onResponse(call: Call<ApiResponse<Any>>?, response: Response<ApiResponse<Any>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.onUserPanicSuccess()
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

    override fun requestCancelRideShareApiCall(orderId: BigInteger) {
        getView()?.showLoader(true)
        RestClient.get().cancelRideShareApi(orderId).enqueue(object : Callback<ApiResponse<Any>> {
            override fun onResponse(call: Call<ApiResponse<Any>>?, response: Response<ApiResponse<Any>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.onCancelRideShareSuccess()
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

    override fun requestRideShareApiCall(json : ShareRideReqModel) {
        getView()?.showLoader(true)
        RestClient.get().shareRideApi(json).enqueue(object : Callback<ApiResponse<RideShareResponse>> {
            override fun onResponse(call: Call<ApiResponse<RideShareResponse>>?, response: Response<ApiResponse<RideShareResponse>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        response.body()?.result?.let { getView()?.onRideShareSuccess(it) }
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<RideShareResponse>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }

    override fun requestVehicleBreakDownApiCall(map: HashMap<String, String>) {
        getView()?.showLoader(true)
        RestClient.get().vehicleBreakdownApi(map).enqueue(object : Callback<ApiResponse<Any>> {

            override fun onResponse(call: Call<ApiResponse<Any>>?, response: Response<ApiResponse<Any>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.onVehiceBreakDownSuccess()
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<Any>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }

    override fun requestHalfWayStopByUserApiCall(map: HashMap<String, String>) {
        getView()?.showLoader(true)
        RestClient.get().halfWayStopByUser(map).enqueue(object : Callback<ApiResponse<Any>> {

            override fun onResponse(call: Call<ApiResponse<Any>>?, response: Response<ApiResponse<Any>>?) {
                getView()?.showLoader(false)
                if (response?.isSuccessful == true) {
                    if (response.body()?.statusCode == 200) {
                        getView()?.onHalfWayStopByUserSuccess()
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<Any>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })

    }

    override fun onGoingOrderApi() {
        getView()?.showLoader(true)
        RestClient.get().onGoingOrder().enqueue(object : Callback<ApiResponse<List<Order>>> {

            override fun onResponse(call: Call<ApiResponse<List<Order>>>?, response: Response<ApiResponse<List<Order>>>?) {
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

            override fun onFailure(call: Call<ApiResponse<List<Order>>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }

    override fun drawPolyLine(sourceLat: Double, sourceLong: Double, destLat: Double, destLong: Double, language: String?) {
        getView()?.showLoader(true)
        val BASE_URL_for_map = "https://maps.googleapis.com/maps/api/"
        val retrofit = Retrofit.Builder().baseUrl(BASE_URL_for_map).addConverterFactory(GsonConverterFactory.create()).build()
        val api = retrofit.create(API::class.java)
        var key ="directions/json?sensor=false&mode=driving&alternatives=true&units=metric&key=" + ConfigPOJO.SECRET_API_KEY
        val service = api.getPolYLine(key,"$sourceLat,$sourceLong", "$destLat,$destLong", language)
        service.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    try {
                        val responsePolyline = response.body()?.string()
                        val jsonRootObject = JSONObject(responsePolyline)
                        getView()?.polyLine(jsonRootObject)
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


    override fun drawPolyLineWithWayPoints(model: Order?, sourceLatLng: LatLng?, destLatLng: LatLng?) {
        getView()?.showLoader(true)
        val BASE_URL_for_map = "https://maps.googleapis.com/maps/api/directions/json?sensor=false&mode=driving&alternatives=true&units=metric&key="+ConfigPOJO.SECRET_API_KEY+"&origin="
        val retrofit = Retrofit.Builder().baseUrl("https://maps.googleapis.com/maps/api/").addConverterFactory(GsonConverterFactory.create()).build()
        val api = retrofit.create(API::class.java)
        val url = StringBuilder("")
        if(model?.ride_stops!!.size>0)
        {
            url.append(BASE_URL_for_map+sourceLatLng?.latitude+","+sourceLatLng?.longitude+"&destination="
                    +destLatLng?.latitude+","+destLatLng?.longitude+"&waypoints=via:")
            for (i in model.ride_stops.indices)
            {
                val coordinates=String.format("%s",model.ride_stops[i].latitude.toString()+","+model.ride_stops[i].longitude+"|via:")
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
                        getView()?.polyLine(jsonRootObject)
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


    override fun getRoadPoints(trackingModel: TrackingModel?) {
        //getView()?.showLoader(true)
        val latlng = trackingModel?.latitude.toString() + "," + trackingModel?.longitude.toString()
        var key = "https://roads.googleapis.com/v1/nearestRoads?key="+ConfigPOJO.SECRET_API_KEY
        RestClient.get().getRoadPoints(key,latlng).enqueue(object : Callback<RoadPoints> {
            override fun onResponse(call: Call<RoadPoints>?,
                                    response: Response<RoadPoints>?) {
                //getView()?.showLoader(false)
                getView()?.snappedPoints((response?.body()?.snappedPoints
                        ?: ArrayList()), trackingModel)
            }

            override fun onFailure(call: Call<RoadPoints>?, t: Throwable?) {
                //getView()?.showLoader(false)
                getView()?.snappedPoints((ArrayList()), trackingModel)
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
                        getView()?.onCancelApiSuccess()
                    } else {
                        getView()?.handleApiError(response.body()?.statusCode, response.body()?.msg)
                    }
                } else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }
            }

            override fun onFailure(call: Call<ApiResponse<Any>>?, t: Throwable?) {
                getView()?.showLoader(false)
                getView()?.apiFailure()
            }
        })
    }

}