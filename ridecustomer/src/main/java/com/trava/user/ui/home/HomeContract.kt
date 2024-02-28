package com.trava.user.ui.home

import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView
import com.trava.utilities.webservices.models.LoginModel
import com.trava.utilities.webservices.models.Service
import com.google.android.gms.maps.model.LatLng
import com.trava.user.webservices.ApiResponseNew
import com.trava.user.webservices.models.GeoFenceData
import com.trava.user.webservices.models.ServiceRequestModel
import com.trava.user.webservices.models.homeapi.ServiceDetails
import org.json.JSONObject

interface HomeContract {

    interface View: BaseView {
        fun onApiSuccess(login: LoginModel?)
        fun logoutSuccess()
        fun onSupportListApiSuccess(response: List<Service>?)
        fun polyLine(jsonRootObject: JSONObject,sourceLatLng: LatLng?, destLatLng: LatLng?)
        fun nearByResponse(jsonRootObject: JSONObject,sourceLatLng: LatLng?, destLatLng: LatLng?)
        fun driverDistanceTimeSuccess(jsonRootObject: JSONObject)
        fun onHomeApiSuccess(response: ServiceDetails?)
        fun onPolygonDataSuccess(response: ArrayList<GeoFenceData>?)
        fun RazorPaymentApiSuccess(apiResponseNew: ApiResponseNew?)
        fun DataThawaniSuccess(apiResponseNew: ApiResponseNew?)
    }

    interface Presenter: BasePresenter<View> {
        fun onGetPolygon()
        fun updateDataApi(map: HashMap<String, String>)
        fun drawPolyLine(sourceLatLng: LatLng?, destLatLng: LatLng?, language: String?)
        fun nearByPlaces(sourceLatLng: LatLng?, destLatLng: LatLng?, language: String?)
        fun drawPolyLineWithWayPoints(model: ServiceRequestModel,sourceLatLng: LatLng?, destLatLng: LatLng?)
        fun logout()
        fun getSupportList()
        fun getDriversDistanceTime(sourceLatLng: LatLng?, destLatLng: LatLng?, language: String?)
        fun homeApi(map: Map<String, String>)
        fun saveRazorPaymentData(map: HashMap<String, Any>)
        fun saveDatatranseData(map: HashMap<String, Any>)
    }

}