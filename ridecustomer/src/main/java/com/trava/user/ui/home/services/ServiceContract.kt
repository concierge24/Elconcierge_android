package com.trava.user.ui.home.services

import com.trava.user.webservices.models.homeapi.HomeDriver
import com.trava.user.webservices.models.homeapi.ServiceDetails
import com.trava.user.webservices.models.homeapi.TerminologyData
import com.trava.utilities.basearc.BasePresenter
import com.trava.utilities.basearc.BaseView

class ServiceContract{

    interface View: BaseView{
        fun onApiSuccess(response: ServiceDetails?)
        fun onApiSuccess(response: TerminologyData?)
        fun snappedDrivers(snappedDrivers: List<HomeDriver>?)
    }

    interface Presenter: BasePresenter<View>{
        fun homeApi(map: Map<String, String>)
        fun terminologyApi(url: String)
        fun getNearestRoadPoints(drivers: List<HomeDriver>?)
    }
}