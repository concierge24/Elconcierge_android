package com.trava.user.ui.home.services

import com.trava.user.utils.ConfigPOJO
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.RestClient
import com.trava.user.webservices.models.homeapi.HomeDriver
import com.trava.user.webservices.models.homeapi.ServiceDetails
import com.trava.user.webservices.models.homeapi.TerminologyData
import com.trava.user.webservices.models.nearestroad.RoadPoints
import com.trava.utilities.DialogIndeterminate
import com.trava.utilities.Logger
import com.trava.utilities.basearc.BasePresenterImpl
import com.trava.utilities.getApiError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ServicePresenter : BasePresenterImpl<ServiceContract.View>(), ServiceContract.Presenter {

    private var api: Call<ApiResponse<ServiceDetails>>? = null
    override fun homeApi(map: Map<String, String>) {
        getView()?.showLoader(true)
        api?.cancel()
        api = RestClient.recreate().get().homeApi(map)
        api?.enqueue(object : Callback<ApiResponse<ServiceDetails>> {

            override fun onResponse(call: Call<ApiResponse<ServiceDetails>>?, response: Response<ApiResponse<ServiceDetails>>?) {
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

            override fun onFailure(call: Call<ApiResponse<ServiceDetails>>?, t: Throwable?) {
                if (t?.message.toString() != "Canceled" && t?.message.toString() != "Socket closed") {
                    getView()?.showLoader(false)
                    getView()?.apiFailure()
                    Logger.e("homeApi?Error", t?.message.toString())
                }
            }
        })
    }

    override fun terminologyApi(url: String)
    {
        var api: Call<ApiResponse<TerminologyData>>? = null
        getView()?.showLoader(true)
        api?.cancel()
        api = RestClient.recreate().get().termsnology("user/service/terminology?category_id=$url")
        api?.enqueue(object : Callback<ApiResponse<TerminologyData>> {

            override fun onResponse(call: Call<ApiResponse<TerminologyData>>?, response: Response<ApiResponse<TerminologyData>>?) {
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

            override fun onFailure(call: Call<ApiResponse<TerminologyData>>?, t: Throwable?) {
                if (t?.message.toString() != "Canceled" && t?.message.toString() != "Socket closed") {
                    getView()?.showLoader(false)
                    getView()?.apiFailure()
                    Logger.e("homeApi?Error", t?.message.toString())
                }
            }
        })
    }

    override fun getNearestRoadPoints(drivers: List<HomeDriver>?) {
        var pointsString = ""
        drivers?.forEach {
            pointsString = "$pointsString${it.latitude},${it.longitude}|"
        }
        pointsString = pointsString.removeSuffix("|")
        var key = "https://roads.googleapis.com/v1/nearestRoads?key="+ ConfigPOJO.SECRET_API_KEY
        RestClient.get().getRoadPoints(key,pointsString).enqueue(object : Callback<RoadPoints> {

            override fun onResponse(call: Call<RoadPoints>?, response: Response<RoadPoints>?) {
                if (response?.isSuccessful == true) {
                    response.body()?.snappedPoints?.forEachIndexed { index, roadItem ->
                        drivers?.get(roadItem.originalIndex?:0)?.latitude = roadItem.location?.latitude
                        drivers?.get(roadItem.originalIndex?:0)?.longitude = roadItem.location?.longitude
                    }
                    getView()?.snappedDrivers(drivers)
                } /*else {
                    val errorModel = getApiError(response?.errorBody()?.string())
                    getView()?.handleApiError(errorModel.statusCode, errorModel.msg)
                }*/
            }

            override fun onFailure(call: Call<RoadPoints>?, t: Throwable?) {
//                if (t?.message.toString() != "Canceled" && t?.message.toString() != "Socket closed") {
//                    getView()?.showLoader(false)
//                    getView()?.apiFailure()
//                    Logger.e("homeApi?Error", t?.message.toString())
//                }
            }

        })
    }

}