package com.trava.user.ui.home.dropofflocation

import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject

interface LocationContract {

    interface View: BaseView {
        fun nearByResponse(jsonRootObject: JSONObject,sourceLatLng: LatLng?, destLatLng: LatLng?)
    }

    interface Presenter: BasePresenter<View> {
        fun nearByPlaces(sourceLatLng: LatLng?, destLatLng: LatLng?, language: String?,type:String)
    }
}