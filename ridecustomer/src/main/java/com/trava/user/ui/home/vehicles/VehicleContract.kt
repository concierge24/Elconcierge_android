package com.trava.user.ui.home.vehicles

import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView
import com.trava.utilities.webservices.models.LoginModel
import com.trava.utilities.webservices.models.Service
import com.google.android.gms.maps.model.LatLng
import com.trava.user.webservices.models.ServiceRequestModel
import com.trava.user.webservices.models.homeapi.ServiceDetails
import org.json.JSONObject

interface VehicleContract {

    interface View: BaseView {
        fun orderDistanceSuccess(jsonRootObject: JSONObject)

    }

    interface Presenter: BasePresenter<View> {
        fun getOrderDistance(sourceLatLng: LatLng?, destLatLng: LatLng?, language: String?)

    }

}