package com.trava.user.ui.home.deliverystarts

import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonObject
import com.trava.user.webservices.models.ServiceRequestModel
import com.trava.user.webservices.models.TrackingModel
import com.trava.user.webservices.models.contacts.ContactReqModel
import com.trava.user.webservices.models.contacts.ContactRequestModel
import com.trava.user.webservices.models.contacts.RideShareResponse
import com.trava.user.webservices.models.contacts.ShareRideReqModel
import com.trava.user.webservices.models.nearestroad.RoadItem
import com.trava.user.webservices.models.order.Order
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView
import org.json.JSONObject
import java.math.BigInteger

class DeliveryStartsContract{

    interface View: BaseView{
        fun onApiSuccess(response: List<Order>?)
        fun polyLine(jsonRootObject: JSONObject)
        fun onCancelApiSuccess()
        fun snappedPoints(response: List<RoadItem>?, trackingModel: TrackingModel?)
        fun onHalfWayStopByUserSuccess()
        fun onVehiceBreakDownSuccess()
        fun onRideShareSuccess(data : RideShareResponse)
        fun onCancelRideShareSuccess()
        fun onUserPanicSuccess()
    }

    interface Presenter: BasePresenter<View>{
        fun onGoingOrderApi()
        fun drawPolyLine(sourceLat: Double, sourceLong: Double, destLat: Double, destLong: Double, language: String?)
        fun drawPolyLineWithWayPoints(model : Order?, sourceLatLng: LatLng?, destLatLng: LatLng?)
        fun requestCancelApiCall(map: HashMap<String, String>)
        fun getRoadPoints(trackingModel: TrackingModel?)
        fun requestHalfWayStopByUserApiCall(map : HashMap<String,String>)
        fun requestVehicleBreakDownApiCall(map : HashMap<String,String>)
        fun requestRideShareApiCall(json : ShareRideReqModel)
        fun requestCancelRideShareApiCall(orderId : BigInteger)
        fun requestPanicApi(map: HashMap<String, Any>)

    }

}